package nonamecrackers2.hunted.entity.ai.behavior;

import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFrom extends Behavior<Mob>
{
	private final MemoryModuleType<LivingEntity> memory;
	private final Function<LivingEntity, Float> speedModifier;
	private final double minDist;
	private final boolean mustSee;
	
	public SetWalkTargetFrom(MemoryModuleType<LivingEntity> memory, float speedModifier, double minDist, boolean mustSee)
	{
		this(memory, e -> speedModifier, minDist, mustSee);
	}
	
	public SetWalkTargetFrom(MemoryModuleType<LivingEntity> memory, Function<LivingEntity, Float> speedModifer, double minDist, boolean mustSee)
	{
		super(ImmutableMap.of(memory, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
		this.memory = memory;
		this.speedModifier = speedModifer;
		this.minDist = minDist;
		this.mustSee = mustSee;
	}
	
	@Override
	protected void start(ServerLevel level, Mob mob, long p_22542_)
	{
		LivingEntity entity = mob.getBrain().getMemory(this.memory).get();
		if (this.mustSee ? BehaviorUtils.canSee(mob, entity) : true && mob.closerThan(entity, this.minDist))
			this.clearWalkTarget(mob);
		else
			this.setWalkAndLookTarget(mob, entity);
	}
	
	private void setWalkAndLookTarget(LivingEntity entity, LivingEntity target)
	{
		Brain<?> brain = entity.getBrain();
		brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
		WalkTarget walkTarget = new WalkTarget(new EntityTracker(target, false), this.speedModifier.apply(entity), 0);
		brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
	}
	
	private void clearWalkTarget(LivingEntity entity)
	{
		entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
	}
}
