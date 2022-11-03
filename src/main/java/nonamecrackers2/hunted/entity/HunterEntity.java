package nonamecrackers2.hunted.entity;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.capability.HunterEntityClassManager;
import nonamecrackers2.hunted.entity.ai.behavior.RandomNodeStroll;
import nonamecrackers2.hunted.entity.ai.behavior.SetWalkTargetFrom;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.game.HuntedGameManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.type.PreyClassType;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.HuntedClassTypes;
import nonamecrackers2.hunted.init.HuntedDataSerializers;
import nonamecrackers2.hunted.init.HuntedMemoryTypes;
import nonamecrackers2.hunted.init.HuntedSensorTypes;
import nonamecrackers2.hunted.init.HuntedSoundEvents;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.map.MapNavigation;
import nonamecrackers2.hunted.util.MobEffectHolder;

public class HunterEntity extends Monster
{
	private static final Logger LOGGER = LogManager.getLogger();
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
	protected static final ImmutableList<SensorType<? extends Sensor<? super HunterEntity>>> SENSOR_TYPES = ImmutableList.of(HuntedSensorTypes.HUNTER_ENTITY_SENSOR.get(), HuntedSensorTypes.NEAREST_GLOWING_ENTITY.get());
	protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.PATH, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, HuntedMemoryTypes.NEAREST_GLOWING_ENTITIES.get(), HuntedMemoryTypes.NEAREST_GLOWING_ENTITY.get(), HuntedMemoryTypes.NEAREST_ATTACKABLE_GLOWING_ENTITY.get(), HuntedMemoryTypes.CURRENT_NODE.get());
	private static final MobEffectHolder PREY_GLOWING = new MobEffectHolder(MobEffects.GLOWING, 60, 0, true);
	private final HunterEntityClassManager classManager = new HunterEntityClassManager(this, HUNTER_ENTITY_CLASS.copy());
	private final DynamicGameEventListener<VibrationListener> vibrationListener;
	private final VibrationListener.VibrationListenerConfig vibrationListenerConfig;
	private List<BlockPos> nodes = Lists.newArrayList();
	private int aggravatedTime;
	private int senseDelay = 200 + this.random.nextInt(120);
	
	public HunterEntity(EntityType<? extends HunterEntity> type, Level level)
	{
		super(type, level);
		this.vibrationListenerConfig = new HunterEntity.HunterVibrationListenerConfig();
		PositionSource positionSource = new EntityPositionSource(this, this.getEyeHeight());
		this.vibrationListener = new DynamicGameEventListener<>(new VibrationListener(positionSource, 16, this.vibrationListenerConfig, null, 0.0F, 20));
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
		if (!this.level.isClientSide)
			this.vibrationListener.getListener().tick(this.level);
		if (this.aggravatedTime > 0)
			this.aggravatedTime--;
		if (this.shouldBeAggrivated())
			this.makeAggravated();
		if (this.senseDelay > 0)
		{
			this.senseDelay--;
			if (this.senseDelay == 0)
			{
				this.senseDelay = 800 + this.random.nextInt(120);
				this.level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
				{
					manager.getCurrentGame().ifPresent(game -> 
					{
						for (LivingEntity active : game.getActiveBy(PreyClassType.class))
							active.addEffect(PREY_GLOWING.createInstance());
						for (LivingEntity living : game.getPlayersBy(PreyClassType.class))
							living.sendSystemMessage(Component.translatable("hunted.inflicted.glowing"));
					});
				});
			}
		}
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
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(400, 650)));
		brain.addActivity(Activity.IDLE, 0, ImmutableList.of(
			new StartAttacking<>(HunterEntity::findNearestValidTarget),
			new RunIf<>(e -> !this.getNodes().isEmpty(), new RandomNodeStroll<>(HunterEntity::getNodes)),
			new RunIf<>(e -> this.getNodes().isEmpty(), new RandomStroll(1.0F, 50, 50))
		));
		brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0, ImmutableList.of(
				new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.25F),
				new MeleeAttack(0),
				new StopAttackingIfTargetInvalid<>(e -> !this.hasLineOfSight(e))
			), 
			MemoryModuleType.ATTACK_TARGET
		);
		brain.addActivityWithConditions(Activity.INVESTIGATE, 
			ImmutableList.of(
				Pair.of(0, new SetWalkTargetFrom(HuntedMemoryTypes.NEAREST_ATTACKABLE_GLOWING_ENTITY.get(), 1.0F, 1, false))
			),
			ImmutableSet.of(Pair.of(HuntedMemoryTypes.NEAREST_ATTACKABLE_GLOWING_ENTITY.get(), MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT))
		);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}
	
	private static Optional<? extends LivingEntity> findNearestValidTarget(HunterEntity entity)
	{
		return entity.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).map(e -> entity.hasLineOfSight(e) ? e : null);
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
	
	public List<BlockPos> getNodes()
	{
		return this.nodes;
	}
	
	public void onGameBegin(HuntedMap map)
	{
		this.setMask(MASKS[this.getRandom().nextInt(MASKS.length)]);
		MapNavigation navigation = map.navigation().orElse(null);
		if (navigation != null)
			this.nodes = navigation.nodes();
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
		this.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.INVESTIGATE, Activity.IDLE));
		super.customServerAiStep();
	}
	
	@Override
	public void checkDespawn() {}
	
	@Override
	public void addAdditionalSaveData(CompoundTag tag)
	{
		super.addAdditionalSaveData(tag);
		VibrationListener.codec(this.vibrationListenerConfig).encodeStart(NbtOps.INSTANCE, this.vibrationListener.getListener()).resultOrPartial(LOGGER::error).ifPresent(data -> {
			tag.put("VibrationListener", data);
		});
		ListTag nodes = new ListTag();
		for (BlockPos node : this.getNodes())
			nodes.add(NbtUtils.writeBlockPos(node));
		tag.put("Nodes", nodes);
		tag.putInt("AggravatedTime", this.aggravatedTime);
		tag.putInt("SenseDelay", this.senseDelay);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag tag)
	{
		super.readAdditionalSaveData(tag);
		if (tag.contains("VibrationListener", 10))
		{
			VibrationListener.codec(this.vibrationListenerConfig).parse(new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("VibrationListener"))).resultOrPartial(LOGGER::error).ifPresent(l -> {
				this.vibrationListener.updateListener(l, this.level);
			});
		}
		ListTag list = tag.getList("Nodes", 10);
		List<BlockPos> nodes = Lists.newArrayList();
		for (int i = 0; i < list.size(); i++)
			nodes.add(NbtUtils.readBlockPos(list.getCompound(i)));
		this.nodes = nodes;
		this.aggravatedTime = tag.getInt("AggravatedTime");
		this.senseDelay = tag.getInt("SenseDelay");
	}
	
	
	@Override
	public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> consumer)
	{
		if (this.level instanceof ServerLevel level)
			consumer.accept(this.vibrationListener, level);
	}
	
	public void makeAggravated()
	{
		this.aggravatedTime = 160;
	}
	
	public boolean shouldBeAggrivated()
	{
		return this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent() || this.getBrain().getMemory(HuntedMemoryTypes.NEAREST_ATTACKABLE_GLOWING_ENTITY.get()).isPresent();
	}
	
	public boolean shouldPlayAmbientSound()
	{
		boolean flag = false;
		Optional<WalkTarget> target = this.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
		if (target.isPresent())
		{
			if (target.get().getTarget().currentPosition().distanceTo(this.position()) > 8.0D)
				flag = true;
		}
		return flag && this.aggravatedTime > 0;
	}
	
	@Override
	protected SoundEvent getAmbientSound()
	{
		return this.shouldPlayAmbientSound() ? HuntedSoundEvents.HUNTER_AMBIENCE.get() : null;
	}
	
	@Override
	public int getAmbientSoundInterval()
	{
		return Math.max(10, this.aggravatedTime / 4);
	}
	
	@Override
	protected float getSoundVolume()
	{
		return 2.5F;
	}
	
	@Override
	public boolean doHurtTarget(Entity entity)
	{
		//float angle = (float)(Mth.atan2(pos.x() - this.getX(), pos.z() - this.getZ()) * (180.0D / Math.PI));
//		float angleDiff = (Mth.wrapDegrees(-this.yBodyRot) + entity.getYHeadRot() + 360) % 360 - 180;
//		if (angleDiff <= 45 && angleDiff >= -45)
//		{
			if (entity instanceof LivingEntity living)
			{
				HuntedGameManager manager = this.level.getCapability(HuntedCapabilities.GAME_MANAGER).orElse(null);
				if (manager != null)
				{
					HuntedGame game = manager.getCurrentGame().orElse(null);
					if (game != null)
					{
						if (game.isActive(living))
						{
							game.eliminate(living);
							return true;
						}
					}
				}
			}
//		}
		return super.doHurtTarget(entity);
	}
	
	private class HunterVibrationListenerConfig implements VibrationListener.VibrationListenerConfig
	{
		@Override
		public boolean shouldListen(ServerLevel level, GameEventListener listener, BlockPos pos, GameEvent event, Context context)
		{
			if (HunterEntity.this.getLevel() == level && !HunterEntity.this.isRemoved() && !HunterEntity.this.isNoAi() && context.sourceEntity() != HunterEntity.this)
			{
				Brain<HunterEntity> brain = HunterEntity.this.getBrain();
				if (brain.getActiveNonCoreActivity().isPresent())
				{
					if (brain.getActiveNonCoreActivity().get() != Activity.FIGHT)
					{
						Optional<LivingEntity> nearestGlowing = brain.getMemory(HuntedMemoryTypes.NEAREST_ATTACKABLE_GLOWING_ENTITY.get());
						if (nearestGlowing.isPresent())
						{
							if (HunterEntity.this.blockPosition().distSqr(pos) < HunterEntity.this.blockPosition().distSqr(nearestGlowing.get().blockPosition()))
								return true;
							else
								return false;
						}
						else
						{
							return true;
						}
					}
					else
					{
						return false;
					}
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}

		@Override
		public void onSignalReceive(ServerLevel level, GameEventListener listener, BlockPos pos, GameEvent event, Entity e, Entity e1, float p_223871_)
		{
			HunterEntity.this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 1.0F, 1));
			HunterEntity.this.makeAggravated();
		}
	}
}
