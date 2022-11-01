package nonamecrackers2.hunted.entity.ai.behavior;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

public class FollowPredeterminedPath extends Behavior<PathfinderMob>
{
	private final Function<LivingEntity, List<BlockPos>> pathGetter;
	private @Nullable BlockPos currentNode;
	
	public FollowPredeterminedPath(Function<LivingEntity, List<BlockPos>> pathGetter) 
	{
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
		this.pathGetter = pathGetter;
	}
	
	protected List<BlockPos> getPath(PathfinderMob mob)
	{
		return this.pathGetter.apply(mob);
	}
	
	@Override
	protected void start(ServerLevel level, PathfinderMob mob, long p_22542_)
	{
		this.currentNode = this.findNextNode(mob);
	}
	
	@Override
	protected void stop(ServerLevel level, PathfinderMob mob, long p_22550_)
	{
		Brain<?> brain = mob.getBrain();
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		this.currentNode = null;
	}
	
	@Override
	protected boolean canStillUse(ServerLevel level, PathfinderMob mob, long p_22547_)
	{
		return !this.getPath(mob).isEmpty() && (this.currentNode != null && this.getPath(mob).contains(this.currentNode) || this.currentNode == null);
	}
	
	@Override
	protected void tick(ServerLevel level, PathfinderMob mob, long p_22553_)
	{
		BlockPos pos = this.currentNode;
		Brain<?> brain = mob.getBrain();
		if (pos != null)
		{
			WalkTarget target = new WalkTarget(pos, (float)mob.getAttributeValue(Attributes.MOVEMENT_SPEED), 2);
			if (mob.distanceToSqr(Vec3.atBottomCenterOf(pos)) > target.getCloseEnoughDist())
			{
				brain.setMemory(MemoryModuleType.WALK_TARGET, target);
			}
			else
			{
				this.currentNode = this.findNextNode(mob);
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.currentNode, (float)mob.getAttributeValue(Attributes.MOVEMENT_SPEED), 2));
			}
		}
		else
		{
			this.currentNode = this.findNextNode(mob);
		}
	}
	
	protected @Nullable BlockPos findNextNode(PathfinderMob mob)
	{
		if (this.currentNode == null)
		{
			return this.getPath(mob).stream().sorted(Comparator.comparingDouble(p -> mob.distanceToSqr(p.getX(), p.getY(), p.getZ()))).findFirst().orElse(null);
		}
		else
		{
			List<BlockPos> path = this.getPath(mob);
			int index = path.indexOf(this.currentNode);
			if (index != -1)
			{
				if (index + 1 < path.size())
					index++;
				else
					index = 0;
				return path.get(index);
			}
			else
			{
				return null;
			}
		}
	}
}
