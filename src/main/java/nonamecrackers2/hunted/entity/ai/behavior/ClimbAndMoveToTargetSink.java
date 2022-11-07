package nonamecrackers2.hunted.entity.ai.behavior;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
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
		
		Path path = mob.getBrain().getMemory(MemoryModuleType.PATH).get();
		
		BlockPos pos = mob.blockPosition();
		BlockState state = level.getBlockState(pos);
		Node next = path.getNextNode();
		BlockPos nextPos = next.asBlockPos();
		if (!path.isDone())
		{
			if (mob.onClimbable())
			{
				if (pos.getY() - nextPos.getY() < 0)
				{
					if ((nextPos.getX() == pos.getX() && nextPos.getZ() == pos.getZ()) || mob.getBoundingBox().inflate(1.0D).contains(next.asVec3()))
					{
						boolean flag = true;
						Direction direction = getDirection(state);
						if (direction != null)
						{
							direction = direction.getOpposite();
							if (!level.getBlockState(new BlockPos(direction.getNormal().offset(mob.blockPosition()))).isAir())
							{
//								System.out.println("climbing; pushing up to wall");
								mob.setDeltaMovement((double)direction.getStepX(), mob.getDeltaMovement().y, (double)direction.getStepZ());
								flag = false;
							}
						}
						
						if (flag)
						{
//							System.out.println("jump climbing");
							mob.getJumpControl().jump();
							//Vec3 center = Vec3.atBottomCenterOf(pos).subtract(mob.position());
							mob.setDeltaMovement(mob.getDeltaMovement().x * 0.1D, mob.getDeltaMovement().y, mob.getDeltaMovement().z * 0.1D);
						}
					}
				}
				else
				{
					boolean flag = true;
					Direction direction = getDirection(state);
					boolean flag2 = !level.getBlockState(pos.below()).is(BlockTags.CLIMBABLE);
					if (direction != null && (level.getBlockState(pos.above(1)).is(BlockTags.CLIMBABLE) && level.getBlockState(new BlockPos(direction.getNormal()).above(Math.round(mob.getBbHeight())).offset(pos)).isAir() || flag2))
					{
//						System.out.println("descending; pushing off ladder");
						double strength = flag2 ? 0.01D : 1.0D;
						mob.setDeltaMovement((double)direction.getStepX()*strength, mob.getDeltaMovement().y, (double)direction.getStepZ()*strength);
						flag = false;
					}
					
					if (flag)
					{
//						System.out.println("descending gracefully");
						Vec3 center = Vec3.atBottomCenterOf(pos).subtract(mob.position());
						mob.setDeltaMovement(center.x, mob.getDeltaMovement().y, center.z);
//						mob.setDeltaMovement(mob.getDeltaMovement().x * 0.1D, mob.getDeltaMovement().y, mob.getDeltaMovement().z * 0.1D);
					}
				}
				
//				if (pos.getY() - nextPos.getY() <= 0)
//				{
//					if ((nextPos.getX() == pos.getX() && nextPos.getZ() == pos.getZ()) || mob.getBoundingBox().inflate(1.0D).contains(next.asVec3()))
//					{
//						boolean flag = true;
//						BlockState state = level.getBlockState(pos);
//						Direction direction = getDirection(state);
//						if (direction != null)
//						{
//							direction = direction.getOpposite();
//							if (!level.getBlockState(new BlockPos(direction.getNormal().offset(mob.blockPosition()))).isAir())
//							{
//								System.out.println("climbing; pushing up to wall");
//								mob.setDeltaMovement((double)direction.getStepX(), mob.getDeltaMovement().y, (double)direction.getStepZ());
//								flag = false;
//							}
//						}
//						
//						if (flag)
//						{
//							System.out.println("jump climbing");
//							mob.getJumpControl().jump();
//							//Vec3 center = Vec3.atBottomCenterOf(pos).subtract(mob.position());
//							mob.setDeltaMovement(mob.getDeltaMovement().x * 0.1D, mob.getDeltaMovement().y, mob.getDeltaMovement().z * 0.1D);
//						}
//					}
//				}
//				else
//				{
//					this.descendClimbable(level, mob, pos, next);
//				}
			}
			else if (!level.getBlockState(pos).is(BlockTags.CLIMBABLE))
			{
				if (pos.getY() - nextPos.getY() < 0 && level.getBlockState(pos.above()).is(BlockTags.CLIMBABLE))
				{
//					System.out.println("trying to jump up to climbable");
					mob.getJumpControl().jump();
					mob.setDeltaMovement(mob.getDeltaMovement().x * 0.1D, mob.getDeltaMovement().y, mob.getDeltaMovement().z * 0.1D);
				}
				else if (level.getBlockState(pos.below()).is(BlockTags.CLIMBABLE))
				{
					mob.setDeltaMovement(mob.getDeltaMovement().x * 0.1D, mob.getDeltaMovement().y, mob.getDeltaMovement().z * 0.1D);
				}
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
