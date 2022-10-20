package nonamecrackers2.hunted.block;


import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import nonamecrackers2.hunted.block.entity.KioskBlockEntity;
import nonamecrackers2.hunted.init.HuntedBlockEntityTypes;

public class KioskBlock extends BaseEntityBlock
{
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final VoxelShape SHAPE_BASE;
	public static final VoxelShape SHAPE_NORTH;
	public static final VoxelShape SHAPE_EAST;
	public static final VoxelShape SHAPE_SOUTH;
	public static final VoxelShape SHAPE_WEST;
	private static final List<Vec3> OFFSETS = ImmutableList.of(
			new Vec3(-0.25, 0.5, 0.31), 
			new Vec3(-0.31, 0.4, 0.19), 
			new Vec3(-0.37, 0.4, -0.25), 
			new Vec3(-0.25, 0.5, -0.32), 
			new Vec3(0.32, 0.45, -0.3), 
			new Vec3(0.32, 0.4, -0.19), 
			new Vec3(0.32, 0.5, 0.12), 
			new Vec3(0.06, 0.3, 0.31), 
			new Vec3(0.36, 1.4, 0.26), 
			new Vec3(0.26, 1.45, 0.26),
			new Vec3(-0.31, 1.5, 0.2), 
			new Vec3(-0.38, 1.4, 0.31)
	);
	
	static 
	{
		VoxelShape shape = Shapes.empty();
		shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.125, 1), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0.25, 0.125, 0.25, 0.75, 0.8125, 0.75), BooleanOp.OR);
		shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0, 1, 1.125, 1), BooleanOp.OR);
		SHAPE_BASE = shape;
		VoxelShape north = Shapes.empty();
		north = Shapes.join(north, Shapes.box(0, 0, 0, 1, 0.125, 1), BooleanOp.OR);
		north = Shapes.join(north, Shapes.box(0.25, 0.125, 0.25, 0.75, 0.875, 0.75), BooleanOp.OR);
		north = Shapes.join(north, Shapes.box(0, 0.5, -0.125, 1, 0.9375, 0.3125), BooleanOp.OR);
		north = Shapes.join(north, Shapes.box(0, 0.625, 0.1875, 1, 1.0625, 0.625), BooleanOp.OR);
		north = Shapes.join(north, Shapes.box(0, 0.75, 0.5, 1, 1.1875, 0.9375), BooleanOp.OR);
		SHAPE_NORTH = north;
		VoxelShape east = Shapes.empty();
		east = Shapes.join(east, Shapes.box(0, 0, 0, 1, 0.125, 1), BooleanOp.OR);
		east = Shapes.join(east, Shapes.box(0.25, 0.125, 0.25, 0.75, 0.875, 0.75), BooleanOp.OR);
		east = Shapes.join(east, Shapes.box(0.6875, 0.5, 0, 1.125, 0.9375, 1), BooleanOp.OR);
		east = Shapes.join(east, Shapes.box(0.375, 0.625, 0, 0.8125, 1.0625, 1), BooleanOp.OR);
		east = Shapes.join(east, Shapes.box(0.0625, 0.75, 0, 0.5, 1.1875, 1), BooleanOp.OR);
		SHAPE_EAST = east;
		VoxelShape south = Shapes.empty();
		south = Shapes.join(south, Shapes.box(0, 0, 0, 1, 0.125, 1), BooleanOp.OR);
		south = Shapes.join(south, Shapes.box(0.25, 0.125, 0.25, 0.75, 0.875, 0.75), BooleanOp.OR);
		south = Shapes.join(south, Shapes.box(0, 0.5, 0.6875, 1, 0.9375, 1.125), BooleanOp.OR);
		south = Shapes.join(south, Shapes.box(0, 0.625, 0.375, 1, 1.0625, 0.8125), BooleanOp.OR);
		south = Shapes.join(south, Shapes.box(0, 0.75, 0.0625, 1, 1.1875, 0.5), BooleanOp.OR);
		SHAPE_SOUTH = south;
		VoxelShape west = Shapes.empty();
		west = Shapes.join(west, Shapes.box(0, 0, 0, 1, 0.125, 1), BooleanOp.OR);
		west = Shapes.join(west, Shapes.box(0.25, 0.125, 0.25, 0.75, 0.875, 0.75), BooleanOp.OR);
		west = Shapes.join(west, Shapes.box(-0.125, 0.5, 0, 0.3125, 0.9375, 1), BooleanOp.OR);
		west = Shapes.join(west, Shapes.box(0.1875, 0.625, 0, 0.625, 1.0625, 1), BooleanOp.OR);
		west = Shapes.join(west, Shapes.box(0.5, 0.75, 0, 0.9375, 1.1875, 1), BooleanOp.OR);
		SHAPE_WEST = west;
	}
	
	public KioskBlock(BlockBehaviour.Properties properties)
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
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
	}
	
	@Override
	public VoxelShape getOcclusionShape(BlockState p_60578_, BlockGetter p_60579_, BlockPos p_60580_)
	{
		return SHAPE_BASE;
	}
	
	@Override
	public boolean useShapeForLightOcclusion(BlockState state)
	{
		return true;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState p_60572_, BlockGetter p_60573_, BlockPos p_60574_, CollisionContext p_60575_)
	{
		return SHAPE_BASE;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_)
	{
		switch (state.getValue(FACING).getOpposite())
		{
		case NORTH:
			return SHAPE_NORTH;
		case SOUTH:
			return SHAPE_SOUTH;
		case EAST:
			return SHAPE_EAST;
		case WEST:
			return SHAPE_WEST;
		default:
			return SHAPE_BASE;
		}
	}
	
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
	{
		for (Vec3 offset : OFFSETS)
		{
			Vec3 particlePos = Vec3.atBottomCenterOf(pos).add(offset.yRot(-state.getValue(FACING).toYRot() * ((float)Math.PI / 180.0F)));
			level.addParticle(ParticleTypes.SMALL_FLAME, particlePos.x, particlePos.y, particlePos.z, 0.0D, 0.0D, 0.0D);
			if (random.nextInt(8) == 0)
				level.playLocalSound(particlePos.x, particlePos.y, particlePos.z, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
		}
	}
	
	public BlockState rotate(BlockState state, Rotation rotation)
	{
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState state, Mirror mirror)
	{
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new KioskBlockEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return level.isClientSide ? createTickerHelper(type, HuntedBlockEntityTypes.KIOSK.get(), KioskBlockEntity::tick) : null;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if (level.isClientSide)
		{
			return InteractionResult.SUCCESS;
		}
		else
		{
			player.openMenu(state.getMenuProvider(level, pos));
			return InteractionResult.CONSUME;
		}
	}
}
