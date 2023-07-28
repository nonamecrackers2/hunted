package nonamecrackers2.hunted.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.game.HuntedGameManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;

public class RewardButtonBlock extends ButtonBlock
{
	protected static final VoxelShape TOP = Shapes.box(0.25, 0.8125, 0.25, 0.75, 1, 0.75);
	protected static final VoxelShape BOTTOM = Shapes.box(0.25, 0, 0.25, 0.75, 0.1875, 0.75);
	protected static final VoxelShape TOP_PRESSED = Shapes.box(0.25, 0.875, 0.25, 0.75, 1, 0.75);
	protected static final VoxelShape BOTTOM_PRESSED = Shapes.box(0.25, 0, 0.25, 0.75, 0.125, 0.75);
	protected static final VoxelShape NORTH = Shapes.box(0.25, 0.25, 0.8125, 0.75, 0.75, 1);
	protected static final VoxelShape NORTH_PRESSED = Shapes.box(0.25, 0.25, 0.875, 0.75, 0.75, 1);
	protected static final VoxelShape EAST = Shapes.box(0, 0.25, 0.25, 0.1875, 0.75, 0.75);
	protected static final VoxelShape EAST_PRESSED = Shapes.box(0, 0.25, 0.25, 0.125, 0.75, 0.75);
	protected static final VoxelShape SOUTH = Shapes.box(0.25, 0.25, 0, 0.75, 0.75, 0.1875);
	protected static final VoxelShape SOUTH_PRESSED = Shapes.box(0.25, 0.25, 0, 0.75, 0.75, 0.125);
	protected static final VoxelShape WEST = Shapes.box(0.8125, 0.25, 0.25, 1, 0.75, 0.75);
	protected static final VoxelShape WEST_PRESSED = Shapes.box(0.875, 0.25, 0.25, 1, 0.75, 0.75);
	
	public RewardButtonBlock(BlockBehaviour.Properties properties)
	{
		super(properties, BlockSetType.DARK_OAK, 20, false);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context)
	{
		Direction facing = state.getValue(ButtonBlock.FACING);
		boolean powered = state.getValue(ButtonBlock.POWERED);
		switch (state.getValue(ButtonBlock.FACE))
		{
		case FLOOR:
			return powered ? BOTTOM_PRESSED : BOTTOM;
		case WALL:
		{
			switch (facing)
			{
			case EAST:
				return powered ? EAST_PRESSED : EAST;
			case SOUTH:
				return powered ? SOUTH_PRESSED : SOUTH;
			case WEST:
				return powered ? WEST_PRESSED : WEST;
			case NORTH:
			default:
				return powered ? NORTH_PRESSED : NORTH;
			}
		}
		case CEILING:
		default:
		{
			return powered ? TOP_PRESSED : TOP;
		}
		}
	}

	@Override
	protected SoundEvent getSound(boolean powered)
	{
		return powered ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF;
	}
	
	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if (state.getValue(ButtonBlock.POWERED))
		{
			return InteractionResult.CONSUME;
		}
		else
		{
			HuntedGameManager manager = level.getCapability(HuntedCapabilities.GAME_MANAGER).orElse(null);
			if (manager != null)
			{
				HuntedGame game = manager.getCurrentGame().orElse(null);
				if (game != null && game.canPressButtons())
				{
					PlayerClassManager classManager = player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
					if (classManager != null)
					{
						HuntedClass huntedClass = classManager.getCurrentClass().orElse(null);
						if (huntedClass != null && huntedClass.getType().canCollectRewards())
						{
							this.press(state, level, pos);
							this.playSound(null, level, pos, true);
							level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
							return InteractionResult.CONSUME;
						}
					}
				}
			}
			return InteractionResult.SUCCESS;
		}
	}
}
