package nonamecrackers2.hunted.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import nonamecrackers2.hunted.entity.HunterEntity;

public class BearTrapBlock extends Block
{
	public static final BlockPathTypes BEARTRAP = BlockPathTypes.create("beartrap", 16.0F);
	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
	private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 6.0D, 15.0D);
	private static final VoxelShape SHAPE_CLOSED = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 9.0D, 15.0D);
	private static final int TRIGGERED_DURATION = 240;
	
	public BearTrapBlock(BlockBehaviour.Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(TRIGGERED, false));
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context)
	{
		if (state.getValue(TRIGGERED))
			return SHAPE_CLOSED;
		else
			return SHAPE;
	}
	
	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
	{
		if (!state.getValue(TRIGGERED) && !(entity instanceof HunterEntity))
		{
			level.setBlock(pos, state.setValue(TRIGGERED, true), 3);
			level.updateNeighborsAt(pos, this);
			level.scheduleTick(pos, this, TRIGGERED_DURATION);
			Player player = null;
			if (entity instanceof Player p)
			{
				player = p;
				p.playSound(SoundEvents.PLAYER_HURT, 1.0F, (p.getRandom().nextFloat() - p.getRandom().nextFloat()) * 0.2F + 1.0F);
			}
			level.playSound(player, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 2.0F, 0.0F);
			if (entity instanceof LivingEntity living)
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false));
		}
	}
	
	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource source)
	{
		if (state.getValue(TRIGGERED))
		{
			level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 0.0F);
			level.setBlock(pos, state.setValue(TRIGGERED, false), 3);
		}
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(TRIGGERED);
	}
	
	@Override
	public @Nullable BlockPathTypes getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob)
	{
		return BEARTRAP;
	}
}
