package nonamecrackers2.hunted.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
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
		i = this.computeLadderNode(pos, nodes, i);
		pos.set(pos.getX(), pos.getY() - 2, pos.getZ());
		i = this.computeLadderNode(pos, nodes, i);
		return i;
	}
	
	private int computeLadderNode(BlockPos pos, Node[] nodes, int nodesSize)
	{
		if (this.level.getBlockState(pos).is(BlockTags.CLIMBABLE))
		{
			Node node = this.getNode(pos);
			if (node != null && !node.closed)
			{
				node.type = BlockPathTypes.WALKABLE;
				node.costMalus = 0.0F;
				if (nodesSize + 1 < nodes.length)
					nodes[nodesSize++] = node;
			}
		}
		return nodesSize;
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
