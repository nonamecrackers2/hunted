package nonamecrackers2.hunted.entity.ai.behavior;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
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
				if (this.wantsToAscend(pos, nextPos))
				{
					if (this.isOnSameVerticalColumn(pos, nextPos) || this.isNearTo(mob, next.asVec3()))
					{
						boolean flag = true;
						Direction direction = getDirection(state);
						if (direction != null)
						{
							direction = direction.getOpposite();
							if (this.isCollidable(level, pos.offset(direction.getNormal())))
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
//					boolean flag = true;
//					Direction direction = getDirection(state);
//					boolean flag2 = !level.getBlockState(pos.below()).is(BlockTags.CLIMBABLE);
//					if (direction != null && (level.getBlockState(pos.above()).is(BlockTags.CLIMBABLE) && level.getBlockState(pos.offset(direction.getNormal()).above(Math.round(mob.getBbHeight()))).isAir() || flag2))
//					{
////						System.out.println("descending; pushing off ladder");
//						double strength = flag2 ? 0.01D : 1.0D;
//						mob.setDeltaMovement((double)direction.getStepX() * strength, mob.getDeltaMovement().y, (double)direction.getStepZ() * strength);
//						flag = false;
//					}
//					
//					if (flag)
//					{
						Vec3 delta = Vec3.atBottomCenterOf(pos).subtract(mob.position());
						BlockPos normal = nextPos.subtract(pos);
						if (!this.isOnSameVerticalColumn(pos, nextPos) && this.isOnSimiliarYLevelAs(mob, nextPos))
						{
							System.out.println("working");
							delta = this.getDeltaFrom(nextPos.subtract(pos));
						}
						mob.setDeltaMovement(delta.x, mob.getDeltaMovement().y, delta.z);
////						mob.setDeltaMovement(mob.getDeltaMovement().x * 0.1D, mob.getDeltaMovement().y, mob.getDeltaMovement().z * 0.1D);
//					}
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
			else if (!state.is(BlockTags.CLIMBABLE))
			{
				if (this.wantsToAscend(pos, nextPos) && this.isClimbable(level, pos.above()))
				{
					mob.getJumpControl().jump();
//					mob.setDeltaMovement(mob.getDeltaMovement().x * 0.1D, mob.getDeltaMovement().y, mob.getDeltaMovement().z * 0.1D);
				}
//				else if (this.isClimbable(level, pos.below()))
//				{
//					mob.setDeltaMovement(mob.getDeltaMovement().x * 0.1D, mob.getDeltaMovement().y, mob.getDeltaMovement().z * 0.1D);
//				}
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
//	
//	protected boolean isSurroundingHorizontalAreaClear(Level level, BlockPos pos)
//	{
//		if (!level.getBlockState(pos.offset(-1, 0, 0)).isAir())
//			return false;
//		else if (!level.getBlockState(pos.offset(1, 0, 0)).isAir())
//			return false;
//		else if (!level.getBlockState(pos.offset(0, 0, -1)).isAir())
//			return false;
//		else if (!level.getBlockState(pos.offset(0, 0, 1)).isAir())
//			return false;
//		else
//			return true;
//	}
	
	protected boolean isOnSameVerticalColumn(BlockPos pos, BlockPos next)
	{
		return pos.getX() == next.getX() && pos.getZ() == next.getZ();
	}
	
	protected boolean isOnSimiliarYLevelAs(Mob mob, BlockPos pos)
	{
		return mob.getY() + (double)mob.getBbHeight() <= (double)pos.getY() || mob.getY() >= (double)pos.getY();
	}
	
	protected boolean isNearTo(Mob mob, Vec3 node)
	{
		return mob.getBoundingBox().inflate(1.0D).contains(node);
	}
	
	protected Vec3 getDeltaFromDirection(Direction direction)
	{
		return this.getDeltaFrom(direction.getNormal());
	}
	
	protected Vec3 getDeltaFrom(Vec3i normal)
	{
		return new Vec3(normal.getX(), 0.0D, normal.getZ());
	}
	
	protected boolean wantsToAscend(BlockPos current, BlockPos next)
	{
		return current.getY() - next.getY() < 0;
	}
	
	protected boolean isCollidable(Level level, BlockPos pos)
	{
		return !level.getBlockState(pos).isAir();
	}
	
	protected boolean isClimbable(Level level, BlockPos pos)
	{
		return level.getBlockState(pos).is(BlockTags.CLIMBABLE);
	}
}
