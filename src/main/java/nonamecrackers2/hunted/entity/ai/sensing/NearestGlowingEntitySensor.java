package nonamecrackers2.hunted.entity.ai.sensing;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import nonamecrackers2.hunted.init.HuntedMemoryTypes;

public class NearestGlowingEntitySensor<T extends LivingEntity> extends NearestLivingEntitySensor<T>
{
	private static final TargetingConditions ATTACKABLE = TargetingConditions.forCombat().range(64.0D).ignoreInvisibilityTesting().ignoreLineOfSight();
	
	@Override
	public Set<MemoryModuleType<?>> requires()
	{
		return ImmutableSet.of(HuntedMemoryTypes.NEAREST_GLOWING_ENTITIES.get(), HuntedMemoryTypes.NEAREST_GLOWING_ENTITY.get(), HuntedMemoryTypes.NEAREST_ATTACKABLE_GLOWING_ENTITY.get());
	}
	
	@Override
	protected void doTick(ServerLevel level, T entity)
	{
		AABB box = entity.getBoundingBox().inflate(this.radiusXZ(), this.radiusY(), this.radiusXZ());
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box, e -> {
			return e != entity && e.isAlive() && e.isCurrentlyGlowing();
		});
		list.sort(Comparator.comparingDouble(entity::distanceToSqr));
		Brain<?> brain = entity.getBrain();
		brain.setMemory(HuntedMemoryTypes.NEAREST_GLOWING_ENTITIES.get(), list);
		brain.setMemory(HuntedMemoryTypes.NEAREST_GLOWING_ENTITY.get(), list.isEmpty() ? null : list.get(0));
		Optional<LivingEntity> nearest = list.stream().filter(e -> {
			return ATTACKABLE.test(entity, e);
		}).findFirst();
		brain.setMemory(HuntedMemoryTypes.NEAREST_ATTACKABLE_GLOWING_ENTITY.get(), nearest);
	}
	
	@Override
	protected int radiusXZ()
	{
		return 64;
	}
	
	@Override
	protected int radiusY()
	{
		return 64;
	}
}
