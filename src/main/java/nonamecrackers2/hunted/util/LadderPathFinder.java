package nonamecrackers2.hunted.util;

import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;

public class LadderPathFinder extends PathFinder
{
	public LadderPathFinder(NodeEvaluator evaluator, int maxVisitedNodes)
	{
		super(evaluator, maxVisitedNodes);
	}
	
	@Override
	protected float distance(Node node, Node node1)
	{
		return node.distanceToXZ(node1);
	}
}
