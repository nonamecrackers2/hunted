package nonamecrackers2.hunted.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import nonamecrackers2.hunted.init.HuntedMemoryTypes;

public class RandomNodeStroll<T extends PathfinderMob> extends Behavior<T>
{
	private final Function<T, List<BlockPos>> nodeGetter;
	private final float speedModifier;
	private final double distance;
	private final int nodeSearchAttempts;
	
	public RandomNodeStroll(Function<T, List<BlockPos>> nodeGetter)
	{
		this(nodeGetter, 1.0F);
	}
	
	public RandomNodeStroll(Function<T, List<BlockPos>> nodeGetter, float speedModifier)
	{
		this(nodeGetter, speedModifier, 16.0D);
	}
	
	public RandomNodeStroll(Function<T, List<BlockPos>> nodeGetter, float speedModifier, double distance)
	{
		this(nodeGetter, speedModifier, distance, 10);
	}
	
	public RandomNodeStroll(Function<T, List<BlockPos>> nodeGetter, float speedModifier, double distance, int searchAttempts) 
	{
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, HuntedMemoryTypes.CURRENT_NODE.get(), MemoryStatus.REGISTERED));
		this.nodeGetter = nodeGetter;
		this.speedModifier = speedModifier;
		this.distance = distance;
		this.nodeSearchAttempts = searchAttempts;
	}
	
	protected List<BlockPos> getNodes(T mob)
	{
		return this.nodeGetter.apply(mob);
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, T entity)
	{
		return !this.getNodes(entity).isEmpty();
	}
	
	@Override
	protected void start(ServerLevel level, T mob, long p_22542_)
	{
		for (int i = 0; i < this.nodeSearchAttempts; i++)
		{
			BlockPos pos = this.findNode(mob);
			if (pos != null)
			{
				mob.getBrain().setMemory(HuntedMemoryTypes.CURRENT_NODE.get(), GlobalPos.of(level.dimension(), pos));
				mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, this.speedModifier, 0));
				return;
			}
		}
		mob.getBrain().eraseMemory(HuntedMemoryTypes.CURRENT_NODE.get());
	}
	
	protected BlockPos findNode(T mob)
	{
		Optional<GlobalPos> currentNode = mob.getBrain().getMemory(HuntedMemoryTypes.CURRENT_NODE.get());
		List<BlockPos> nodes = this.getNodes(mob).stream().filter(p -> 
		{
			if (currentNode.isPresent())
			{
				BlockPos pos = currentNode.get().pos();
				if (!pos.equals(p) && pos.distSqr(p) > this.distance*this.distance)
					return true;
				else
					return false;
			}
			else
			{
				return true;
			}
		}).collect(Collectors.toList());
		
		if (nodes.size() > 0)
			return nodes.get(mob.getRandom().nextInt(nodes.size()));
		else
			return null;
	}
}
