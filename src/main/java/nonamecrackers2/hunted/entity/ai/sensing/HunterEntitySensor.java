package nonamecrackers2.hunted.entity.ai.sensing;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import nonamecrackers2.hunted.entity.HunterEntity;

public class HunterEntitySensor extends NearestLivingEntitySensor<HunterEntity>
{
	@Override
	public Set<MemoryModuleType<?>> requires()
	{
		return ImmutableSet.copyOf(Iterables.concat(super.requires(), Lists.newArrayList(MemoryModuleType.NEAREST_ATTACKABLE)));
	}
	
	@Override
	protected void doTick(ServerLevel level, HunterEntity entity)
	{
		super.doTick(level, entity);
		getClosest(entity, e -> {
			return e.getType() == EntityType.PLAYER && e.hasLineOfSight(entity) && !e.isInvisible() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e);
		}).ifPresentOrElse(e -> {
			entity.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, e);
		}, () -> {
			entity.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE);
		});
	}
	
	private static Optional<LivingEntity> getClosest(HunterEntity hunter, Predicate<LivingEntity> predicate)
	{
		return hunter.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).stream().flatMap(Collection::stream).filter(predicate).findFirst();
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
