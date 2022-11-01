package nonamecrackers2.hunted.entity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.capability.HunterEntityClassManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.HuntedClassTypes;
import nonamecrackers2.hunted.init.HuntedDataSerializers;
import nonamecrackers2.hunted.init.HuntedSoundEvents;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.map.MapNavigation;

public class HunterEntity extends Monster
{
	public static final HuntedClass HUNTER_ENTITY_CLASS = new HuntedClass(
			HuntedMod.resource("hunter_entity_class"), 
			CommonComponents.EMPTY, 
			CommonComponents.EMPTY, 
			HuntedClassTypes.HUNTER.get(), 
			Optional.of(HuntedSoundEvents.HEARTBEAT.get()), 
			true, 
			null, 
			ImmutableList.of(), 
			ImmutableList.of(),
			ImmutableMap.of(), 
			null
	);
	public static final ResourceLocation[] MASKS = new ResourceLocation[] {
			HuntedMod.resource("textures/mask/mask.png"),
			HuntedMod.resource("textures/mask/mask2.png"),
			HuntedMod.resource("textures/mask/mask3.png"),
			HuntedMod.resource("textures/mask/mask4.png"),
			HuntedMod.resource("textures/mask/mask5.png"),
			HuntedMod.resource("textures/mask/mask6.png")
	};
	private static final EntityDataAccessor<ResourceLocation> MASK = SynchedEntityData.defineId(HunterEntity.class, HuntedDataSerializers.RESOURCE_LOCATION.get());
	private static final EntityDataAccessor<Boolean> IS_IN_GAME = SynchedEntityData.defineId(HunterEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> HAS_ESCAPED = SynchedEntityData.defineId(HunterEntity.class, EntityDataSerializers.BOOLEAN);
	protected static final ImmutableList<SensorType<? extends Sensor<? super HunterEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS, SensorType.NEAREST_LIVING_ENTITIES);
	protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.PATH, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);
	private final HunterEntityClassManager classManager = new HunterEntityClassManager(this, HUNTER_ENTITY_CLASS.copy());
	private List<BlockPos> path = Lists.newArrayList();
	private int currentPathIndex;
	
	public HunterEntity(EntityType<? extends HunterEntity> type, Level level)
	{
		super(type, level);
		this.setInvulnerable(true);
	}
	
	public static AttributeSupplier.Builder createAttributes()
	{
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 64.0D).add(Attributes.MOVEMENT_SPEED, 0.3D).add(Attributes.FOLLOW_RANGE, 128.0D);
	}
	
	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		this.entityData.define(MASK, MASKS[0]);
		this.entityData.define(IS_IN_GAME, false);
		this.entityData.define(HAS_ESCAPED, false);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		this.level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
		{
			manager.getCurrentGame().ifPresent(game -> 
			{
				this.entityData.set(IS_IN_GAME, game.isActive(this));
				this.entityData.set(HAS_ESCAPED, game.hasPlayerEscaped(this));
			});
		});
	}
	
	public boolean isInGame()
	{ 
		return this.entityData.get(IS_IN_GAME);
	}
	
	public boolean hasEscaped()
	{
		return this.entityData.get(HAS_ESCAPED);
	}
	
	@Override
	protected Brain.Provider<HunterEntity> brainProvider()
	{
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}
	
	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic)
	{
		Brain<HunterEntity> brain = this.brainProvider().makeBrain(dynamic);
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
		brain.addActivity(Activity.IDLE, 0, ImmutableList.of(
			//new RunIf<>((e) -> !this.getPath().isEmpty(), new FollowPredeterminedPath((l) -> this.getPath())), 
			new StartAttacking<>(HunterEntity::findNearestValidTarget),
			new RandomStroll(0.8F, 50, 50)
		));
		//brain.addActivityWithConditions(Activity.INVESTIGATE, ImmutableList.of(), ImmutableSet.of());
		brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0, ImmutableList.of(
				new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F),
				new MeleeAttack(30),
				new StopAttackingIfTargetInvalid<>()
			), MemoryModuleType.ATTACK_TARGET
		);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}
	
	private static Optional<? extends LivingEntity> findNearestValidTarget(HunterEntity entity)
	{
		return entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Brain<HunterEntity> getBrain()
	{
		return (Brain<HunterEntity>)super.getBrain();
	}
	
	public ResourceLocation getMask()
	{
		return this.entityData.get(MASK);
	}
	
	public void setMask(ResourceLocation mask)
	{
		this.entityData.set(MASK, mask);
	}
	
	public List<BlockPos> getPath()
	{
		return this.path;
	}
	
	public int getCurrentPathIndex()
	{
		return this.currentPathIndex;
	}
	
	public void onGameBegin(HuntedMap map)
	{
		this.setMask(MASKS[this.getRandom().nextInt(MASKS.length)]);
		MapNavigation navigation = map.navigation().orElse(null);
		if (navigation != null)
			this.path = navigation.path();
		BlockPos closest = this.path.stream().sorted(Comparator.comparingDouble(p -> this.distanceToSqr(p.getX(), p.getY(), p.getZ()))).findFirst().orElse(null);
		if (closest != null)
		{
			for (int i = 0; i < this.path.size(); i++)
			{
				if (this.path.get(i).equals(closest))
				{
					this.currentPathIndex = i;
					break;
				}
			}
		}
	}
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap)
	{
		if (cap == HuntedCapabilities.PLAYER_CLASS_MANAGER)
			return LazyOptional.of(() -> this.classManager).cast();
		else
			return super.getCapability(cap);
	}
	
	@Override
	protected void tickDeath()
	{
		if (!this.isRemoved() && !this.level.isClientSide)
			this.remove(RemovalReason.KILLED);
	}
	
	@Override
	public int getTeamColor()
	{
		HuntedClass huntedClass = this.classManager.getCurrentClass().orElse(null);
		if (huntedClass != null)
			return huntedClass.getType().getColor();
		return super.getTeamColor();
	}
	
	@Override
	protected void customServerAiStep()
	{
		this.level.getProfiler().push("hunterBrain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		this.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
		super.customServerAiStep();
		System.out.println(this.getBrain().getRunningBehaviors());
	}
	
	@Override
	public void checkDespawn() {}
}
