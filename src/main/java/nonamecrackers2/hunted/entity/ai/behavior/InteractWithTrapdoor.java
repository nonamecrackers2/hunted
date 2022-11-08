package nonamecrackers2.hunted.entity.ai.behavior;

import java.util.Iterator;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import nonamecrackers2.hunted.init.HuntedMemoryTypes;
import nonamecrackers2.hunted.mixin.MixinTrapDoorBlock;

public class InteractWithTrapdoor extends Behavior<LivingEntity>
{
	private Node lastNode;
	private int cooldown;
	
	public InteractWithTrapdoor()
	{
		super(ImmutableMap.of(MemoryModuleType.PATH, MemoryStatus.VALUE_PRESENT, HuntedMemoryTypes.TRAP_DOORS_TO_CLOSE.get(), MemoryStatus.REGISTERED));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity)
	{
		Path path = entity.getBrain().getMemory(MemoryModuleType.PATH).get();
		if (!path.notStarted() && !path.isDone())
		{
			if (!Objects.equals(this.lastNode, path.getNextNode()))
			{
				this.cooldown = 20;
				return true;
			}
			else
			{
				if (this.cooldown > 0)
					this.cooldown--;
				return this.cooldown == 0;
			}
		}
		else
		{
			return false;
		}
	}
	
	@Override
	protected void start(ServerLevel level, LivingEntity entity, long time)
	{
		Path path = entity.getBrain().getMemory(MemoryModuleType.PATH).get();
		this.lastNode = path.getNextNode();
		Node prev = path.getPreviousNode();
		Node next = path.getNextNode();
		int height = Math.round(entity.getBbHeight());
		for (int i = 0; i < height; i++)
		{
			this.interactWithTrapdoorAt(level, entity, prev.asBlockPos().above(i), false);
			this.interactWithTrapdoorAt(level, entity, next.asBlockPos().above(i), true);
		}
		this.closePassedOrOpenedTrapDoors(level, entity, prev, next);
	}
	
	protected void interactWithTrapdoorAt(ServerLevel level, LivingEntity entity, BlockPos pos, boolean rememberIfClosed)
	{
		BlockState state = level.getBlockState(pos);
		if (state.is(BlockTags.WOODEN_TRAPDOORS, block -> block.getBlock() instanceof TrapDoorBlock))
		{
			if (this.isTrapdoorObstructing(level, entity, state, pos))
			{
				cycleTrapdoor(level, (TrapDoorBlock)state.getBlock(), state, pos);
				if (rememberIfClosed)
					this.rememberTrapDoor(level, entity, pos);
			}
			
			if (!rememberIfClosed)
				this.rememberTrapDoor(level, entity, pos);
		}
	}
	
	protected void closePassedOrOpenedTrapDoors(ServerLevel level, LivingEntity entity, Node prev,  Node next)
	{
		Brain<?> brain = entity.getBrain();
		if (brain.hasMemoryValue(HuntedMemoryTypes.TRAP_DOORS_TO_CLOSE.get()))
		{
			Iterator<GlobalPos> trapdoors = brain.getMemory(HuntedMemoryTypes.TRAP_DOORS_TO_CLOSE.get()).get().iterator();
			while (trapdoors.hasNext())
			{
				GlobalPos globalPos = trapdoors.next();
				BlockPos pos = globalPos.pos();
				if (!(prev.asBlockPos().getX() == pos.getX() && prev.asBlockPos().getZ() == pos.getZ()) && !(next.asBlockPos().getX() == pos.getX() && next.asBlockPos().getZ() == pos.getZ()))
				{
					if (this.isTrapDoorTooFarAway(level, entity, globalPos))
					{
						trapdoors.remove();
					}
					else
					{
						BlockState state = level.getBlockState(pos);
						if (!state.is(BlockTags.WOODEN_TRAPDOORS, block -> block.getBlock() instanceof TrapDoorBlock))
						{
							trapdoors.remove();
						}
						else
						{
							TrapDoorBlock block = (TrapDoorBlock)state.getBlock();
							if (this.isTrapdoorObstructing(level, entity, state, pos))
							{
								trapdoors.remove();
							}
							else
							{
								cycleTrapdoor(level, block, state, pos);
								trapdoors.remove();
							}
						}
					}
				}
			}
		}
	}
	
	protected boolean isTrapDoorTooFarAway(ServerLevel level, LivingEntity entity, GlobalPos pos)
	{
		return pos.dimension() != level.dimension() || !pos.pos().closerToCenterThan(entity.position(), 2.0D);
	}
	
	protected void rememberTrapDoor(ServerLevel level, LivingEntity entity, BlockPos pos)
	{
		Brain<?> brain = entity.getBrain();
		GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
		if (brain.getMemory(HuntedMemoryTypes.TRAP_DOORS_TO_CLOSE.get()).isPresent())
			brain.getMemory(HuntedMemoryTypes.TRAP_DOORS_TO_CLOSE.get()).get().add(globalPos);
		else
			brain.setMemory(HuntedMemoryTypes.TRAP_DOORS_TO_CLOSE.get(), Sets.newHashSet(globalPos));
	}
	
	protected boolean isTrapdoorObstructing(ServerLevel level, LivingEntity entity, BlockState state, BlockPos pos)
	{
		Direction direction = state.getValue(TrapDoorBlock.FACING).getOpposite();
		if (entity.getY() <= (double)pos.getY() + 0.5D && entity.getY() + entity.getBbHeight() >= (double)pos.getY() + 0.5D && level.getBlockState(pos.relative(direction)).isAir())
			return state.getValue(TrapDoorBlock.OPEN);
		else
			return !state.getValue(TrapDoorBlock.OPEN);
	}
	
	private static void cycleTrapdoor(ServerLevel level, TrapDoorBlock block, BlockState state, BlockPos pos)
	{
		state = state.cycle(TrapDoorBlock.OPEN);
		level.setBlock(pos, state, 2);
		if (state.getValue(TrapDoorBlock.WATERLOGGED))
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		((MixinTrapDoorBlock)block).callPlaySound(null, level, pos, state.getValue(TrapDoorBlock.OPEN));
	}
}
