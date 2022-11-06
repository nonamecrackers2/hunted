package nonamecrackers2.hunted.entity.ai.behavior;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class ClimbAndMoveToTargetSink extends MoveToTargetSink
{
	public ClimbAndMoveToTargetSink(int chanceMin, int chanceMax)
	{
		super(chanceMin, chanceMax);
	}
	
	@Override
	protected void tick(ServerLevel level, Mob mob, long p_23619_)
	{
		super.tick(level, mob, p_23619_);
		
		Path path = mob.getNavigation().getPath();
		if (mob.onClimbable())
		{
			if (path != null && !path.isDone())
			{
				BlockPos current = mob.blockPosition();
				BlockPos next = path.getNextNodePos();
				if (current.getY() - next.getY() <= 0)
				{
					if (mob.getBoundingBox().inflate(0.0D, 1.0D, 0.0D).contains(Vec3.atCenterOf(next)))
					{
						boolean flag = true;
						BlockState state = level.getBlockState(current);
						Direction direction = getDirection(state);
						if (direction != null)
						{
							direction = direction.getOpposite();
							if (!level.getBlockState(new BlockPos(direction.getNormal()).offset(mob.blockPosition())).isAir())
							{
								mob.setDeltaMovement((double)direction.getStepX(), mob.getDeltaMovement().y, (double)direction.getStepZ());
								flag = false;
							}
						}
						
						if (flag)
						{
							mob.getJumpControl().jump();
							Vec3 center = Vec3.atBottomCenterOf(current).subtract(mob.position());
							mob.setDeltaMovement(center.x, mob.getDeltaMovement().y, center.z);
						}
					}
				}
				else
				{
					this.descendClimbable(level, mob);
				}
			}
			else
			{
				this.descendClimbable(level, mob);
			}
		}
	}

	private void descendClimbable(ServerLevel level, Mob mob)
	{
		boolean flag = true;
		BlockState state = level.getBlockState(mob.blockPosition());
		Direction direction = getDirection(state);
		BlockPos pos = mob.blockPosition();
		if (direction != null)
		{
			if (level.getBlockState(pos.above(1)).is(BlockTags.CLIMBABLE) && level.getBlockState(new BlockPos(direction.getNormal()).offset(pos)).isAir())
			{
				mob.setDeltaMovement(direction.getStepX(), mob.getDeltaMovement().y, direction.getStepY());
				flag = false;
			}
		}
		
		if (flag)
		{
			Vec3 center = Vec3.atBottomCenterOf(pos).subtract(mob.position());
			mob.setDeltaMovement(center.x, mob.getDeltaMovement().y, center.z);
		}
	}
	
	private static @Nullable Direction getDirection(BlockState state)
	{
		if (state.hasProperty(HorizontalDirectionalBlock.FACING))
		{
			Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
			if (direction != Direction.UP && direction != Direction.DOWN)
				return state.getValue(HorizontalDirectionalBlock.FACING);
		}
		if (state.hasProperty(PipeBlock.NORTH))
		{
			if (state.getValue(PipeBlock.NORTH))
				return Direction.SOUTH;
		}
		if (state.hasProperty(PipeBlock.EAST))
		{
			if (state.getValue(PipeBlock.EAST))
				return Direction.WEST;
		}
		if (state.hasProperty(PipeBlock.SOUTH))
		{
			if (state.getValue(PipeBlock.SOUTH))
				return Direction.NORTH;
		}
		if (state.hasProperty(PipeBlock.WEST))
		{
			if (state.getValue(PipeBlock.WEST))
				return Direction.EAST;
		}
		return null;
	}
}
