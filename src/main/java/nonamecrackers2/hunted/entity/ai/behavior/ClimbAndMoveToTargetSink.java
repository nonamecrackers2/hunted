package nonamecrackers2.hunted.entity.ai.behavior;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

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
		if (path != null && mob.onClimbable())
		{
			boolean flag = true;
			BlockState state = level.getBlockState(mob.blockPosition());
			Direction direction = getDirection(state);
			if (direction != null)
			{
				direction = direction.getOpposite();
				if (!level.getBlockState(new BlockPos(direction.getStepX(), 0, direction.getStepZ()).offset(mob.blockPosition())).isAir())
				{
					mob.setDeltaMovement((double)direction.getStepX(), mob.getDeltaMovement().y, (double)direction.getStepZ());
					flag = false;
				}
			}
			
			if (flag)
			{
				mob.getJumpControl().jump();
				mob.setDeltaMovement(mob.getDeltaMovement().x * 0.1D, mob.getDeltaMovement().y, mob.getDeltaMovement().z * 0.1D);
			}
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
		else if (state.hasProperty(PipeBlock.NORTH))
		{
			if (state.getValue(PipeBlock.NORTH))
				return Direction.NORTH;
		}
		else if (state.hasProperty(PipeBlock.EAST))
		{
			if (state.getValue(PipeBlock.EAST))
				return Direction.EAST;
		}
		else if (state.hasProperty(PipeBlock.SOUTH))
		{
			if (state.getValue(PipeBlock.SOUTH))
				return Direction.SOUTH;
		}
		else if (state.hasProperty(PipeBlock.WEST))
		{
			if (state.getValue(PipeBlock.WEST))
				return Direction.WEST;
		}
		return null;
	}
}
