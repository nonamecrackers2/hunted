package nonamecrackers2.hunted.util;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class ClimbableNodeEvaluator extends WalkNodeEvaluator
{
	@Override
	public int getNeighbors(Node[] nodes, Node origin)
	{
		int i = super.getNeighbors(nodes, origin);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(origin.x, origin.y + 1, origin.z);
		BlockState state = this.level.getBlockState(pos);
		if (state.is(BlockTags.CLIMBABLE))
			i = this.computeAcceptedNode(pos, nodes, i);
		
		pos.set(pos.getX(), pos.getY() - 2, pos.getZ());
		state = this.level.getBlockState(pos);
		if (state.is(BlockTags.CLIMBABLE) || state.is(BlockTags.WOODEN_TRAPDOORS))
			i = this.computeAcceptedNode(pos, nodes, i);
		
		pos.set(pos.getX(), origin.y, pos.getZ());
		if (this.level.getBlockState(pos).is(BlockTags.CLIMBABLE))
		{
			pos.set(pos.getX(), pos.getY() + 1, pos.getZ());
			if (this.mob.getPathfindingMalus(this.getCachedBlockType(this.mob, pos.getX(), pos.getY(), pos.getZ())) == 0.0F)
				i = this.computeAcceptedNode(pos, nodes, i);
		}
		return i;
	}
	
	private int computeAcceptedNode(BlockPos pos, Node[] nodes, int nodesSize)
	{
		Node node = this.getNode(pos);
		if (node != null && !node.closed)
		{
			node.type = BlockPathTypes.WALKABLE;
			node.costMalus = 0.0F;
			if (nodesSize + 1 < nodes.length)
				nodes[nodesSize++] = node;
		}
		return nodesSize;
	}
	
	@Override
	public Node getStart()
	{
		return this.getStartNode(this.mob.blockPosition());
	}
	
	@Override
	protected Node getStartNode(BlockPos pos)
	{
		Node node = super.getStartNode(pos);
		if (node == null || node.costMalus > 0.0F)
		{
			if (this.level.getBlockState(pos).is(BlockTags.CLIMBABLE))
			{
				node = this.getNode(pos);
				node.type = BlockPathTypes.WALKABLE;
				node.costMalus = 0.0F;
			}
		}
		return node;
	}
//	
//	@Override
//	public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z, Mob mob, int height, int width, int deoth, boolean canOpenDoors, boolean canPassDoors)
//	{
//		return super.getBlockPathType(level, x, y, z, mob, height, width, deoth, canOpenDoors, canPassDoors);
//	}
}
