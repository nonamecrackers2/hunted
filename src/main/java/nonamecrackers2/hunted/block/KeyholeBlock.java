package nonamecrackers2.hunted.block;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import nonamecrackers2.hunted.block.entity.KeyholeBlockEntity;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.TriggerTypes;
import nonamecrackers2.hunted.trigger.TriggerContext;

public class KeyholeBlock extends HorizontalDirectionalBlock implements EntityBlock
{
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	
	public KeyholeBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new KeyholeBlockEntity(pos, state);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		BlockEntity entity = level.getBlockEntity(pos);
		if (!level.isClientSide)
		{
			PlayerClassManager manager = player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
			boolean flag2 = true;
			if (manager != null && manager.getCurrentClass().isPresent())
			{
				if (!manager.getCurrentClass().get().getType().canCollectRewards())
					flag2 = false;
			}
			if (flag2)
			{
				if (entity instanceof KeyholeBlockEntity keyhole)
				{
					boolean flag = false;
					if (!keyhole.getItem().isEmpty())
					{
						if (player.getItemInHand(hand).isEmpty())
							player.setItemInHand(hand, keyhole.getItem());
						else
							player.addItem(keyhole.getItem());
						keyhole.setItem(ItemStack.EMPTY);
						flag = true;
					}
					ItemStack stack = player.getItemInHand(hand);
					boolean flag1 = true;
					if (stack.getOrCreateTag().contains("HuntedGameData"))
						flag1 = !stack.getTag().getCompound("HuntedGameData").contains("Ability");
					if (!stack.isEmpty() && flag1 && !flag)
					{
						ItemStack toPut = stack.split(1);
						keyhole.setItem(toPut);
						if (!level.isClientSide)
						{
							level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(gameManager -> 
							{
								gameManager.getCurrentGame().ifPresent(game -> {
									game.trigger(TriggerTypes.KEYHOLE.get(), TriggerContext.builder().player((ServerPlayer)player).hand(hand).item(toPut).result(result));
								});
							});
						}
						flag = true;
					}
					if (flag)
					{
						level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
						return InteractionResult.SUCCESS;
					}
					else
					{
						return InteractionResult.PASS;
					}
				}
			}
		}
		
		return InteractionResult.CONSUME;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state1, boolean bool)
	{
		if (!state.is(state1.getBlock()))
		{
			BlockEntity entity = level.getBlockEntity(pos);
			if (entity instanceof KeyholeBlockEntity keyhole)
			{
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), keyhole.getItem());
				keyhole.setItem(ItemStack.EMPTY);
			}
		}
		super.onRemove(state, level, pos, state1, bool);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
	}
}
