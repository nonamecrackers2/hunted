package nonamecrackers2.hunted.map.event;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.HuntedUtil;

public class GateEvent extends MapEvent<GateEvent.Settings>
{
	public static final Codec<GateEvent.Settings> CODEC = RecordCodecBuilder.create(instance -> 
	{
		return instance.group(
				ForgeRegistries.BLOCKS.getCodec().fieldOf("gate_block").forGetter(GateEvent.Settings::gateBlock),
				BlockPos.CODEC.fieldOf("gate_min").forGetter(GateEvent.Settings::gateMin),
				BlockPos.CODEC.fieldOf("gate_max").forGetter(GateEvent.Settings::gateMax),
				Codec.INT.optionalFieldOf("y_level_delay").forGetter(s -> Optional.of(s.delayBetweenY)),
				StringRepresentable.fromEnum(GateEvent.OpenType::values).optionalFieldOf("open_type").forGetter(s -> Optional.of(s.openType)))
				.apply(instance, (gateBlock, gateMin, gateMax, delay, openType) -> new GateEvent.Settings(gateBlock, gateMin, gateMax, delay.orElse(20), openType.orElse(GateEvent.OpenType.BOTTOM_TOP)));
	});
	public static final String Y_DELAY = "GateOpenYDelay";
	public static final String GATE_Y = "GateY";
	
	public GateEvent()
	{
		super(CODEC);
	}
	
	@Override
	public void activate(GateEvent.Settings settings, TriggerContext context, CompoundTag tag)
	{
		BoundingBox box = BoundingBox.fromCorners(settings.gateMin, settings.gateMax);
		context.level().playSound(null, box.getCenter(), SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 300.0F, 0.0F);
		tag.putBoolean("IsOpening", true);
		if (settings.openType() == GateEvent.OpenType.INSTANT)
		{
			for (BlockPos pos : BlockPos.betweenClosed(settings.gateMin, settings.gateMax))
			{
				BlockState state = context.level().getBlockState(pos);
				if (state.is(settings.gateBlock))
				{
					context.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
					context.level().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10, 0.2D, 0.2D, 0.2D, 0.05D);
					context.level().playSound(null, pos, state.getSoundType(context.level(), pos, null).getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
				}
			}
		}
	}
	
	@Override
	public void begin(GateEvent.Settings settings, ServerLevel level, HuntedGame game, CompoundTag tag) 
	{
		resetMap(settings, level, game);
	}
	
	@Override
	public void tick(GateEvent.Settings settings, ServerLevel level, HuntedGame game, CompoundTag tag) 
	{
		if (tag.getBoolean("IsOpening"))
			settings.openType().tick(settings, level, game, tag);
	}
	
	@Override
	public void reset(GateEvent.Settings settings, ServerLevel level, HuntedGame game, CompoundTag tag) 
	{
		resetMap(settings, level, game);
	}
	
	private static void resetMap(GateEvent.Settings settings, ServerLevel level, HuntedGame game)
	{
		Iterable<BlockPos> blocks = BlockPos.betweenClosed(settings.gateMin, settings.gateMax);
		for (BlockPos pos : blocks)
		{
			BlockState state = level.getBlockState(pos);
			if (state.isAir())
				level.setBlockAndUpdate(pos, settings.gateBlock.defaultBlockState());
		}
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(GateEvent.Settings settings)
	{
		return Trigger.criteria();
	}
	
	protected static record Settings(Block gateBlock, BlockPos gateMin, BlockPos gateMax, int delayBetweenY, GateEvent.OpenType openType) {}
	
	public static enum OpenType implements StringRepresentable
	{
		INSTANT("instant")
		{
			@Override
			public void tick(Settings settings, ServerLevel level, HuntedGame game, CompoundTag tag) {}
		},
		BOTTOM_TOP("bottom_top")
		{
			@Override
			public void tick(Settings settings, ServerLevel level, HuntedGame game, CompoundTag tag)
			{
				List<BlockPos> blocks = BlockPos.betweenClosedStream(settings.gateMin, settings.gateMax).collect(Collectors.mapping(BlockPos::immutable, Collectors.toList()));
				BlockPos minY = blocks.stream().sorted((p, p1) -> p.getY() - p1.getY()).findFirst().orElse(null);
				BlockPos maxY = blocks.stream().sorted((p, p1) -> p1.getY() - p.getY()).findFirst().orElse(null);
				if (minY != null && maxY != null)
				{
					HuntedUtil.count(tag, Y_DELAY);
					if (tag.getInt(Y_DELAY) > settings.delayBetweenY)
					{
						tag.putInt(Y_DELAY, 0);
						int y = HuntedUtil.count(tag, GATE_Y) - 1 + minY.getY();
						if (y <= maxY.getY())
						{
							List<BlockPos> layer = blocks.stream().filter(pos -> pos.getY() == y).collect(Collectors.toList());
							for (BlockPos pos : layer)
							{
								BlockState state = level.getBlockState(pos);
								if (state.is(settings.gateBlock))
								{
									level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
									level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10, 0.2D, 0.2D, 0.2D, 0.05D);
									level.playSound(null, pos, state.getSoundType(level, pos, null).getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
								}
							}
							BoundingBox box = BoundingBox.fromCorners(settings.gateMin, settings.gateMax);
							level.playSound(null, box.getCenter(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 300.0F, 0.0F);
						}
					}
				}
			}
		},
		TOP_BOTTOM("top_bottom")
		{
			@Override
			public void tick(Settings settings, ServerLevel level, HuntedGame game, CompoundTag tag)
			{
				List<BlockPos> blocks = BlockPos.betweenClosedStream(settings.gateMin, settings.gateMax).collect(Collectors.mapping(BlockPos::immutable, Collectors.toList()));
				BlockPos minY = blocks.stream().sorted((p, p1) -> p.getY() - p1.getY()).findFirst().orElse(null);
				BlockPos maxY = blocks.stream().sorted((p, p1) -> p1.getY() - p.getY()).findFirst().orElse(null);
				if (minY != null && maxY != null)
				{
					HuntedUtil.count(tag, Y_DELAY);
					if (tag.getInt(Y_DELAY) > settings.delayBetweenY)
					{
						tag.putInt(Y_DELAY, 0);
						int y = HuntedUtil.count(tag, GATE_Y, true) + 1 + maxY.getY();
						if (y >= minY.getY())
						{
							List<BlockPos> layer = blocks.stream().filter(pos -> pos.getY() == y).collect(Collectors.toList());
							for (BlockPos pos : layer)
							{
								BlockState state = level.getBlockState(pos);
								if (state.is(settings.gateBlock))
								{
									level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
									level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10, 0.2D, 0.2D, 0.2D, 0.05D);
									level.playSound(null, pos, state.getSoundType(level, pos, null).getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
								}
							}
							BoundingBox box = BoundingBox.fromCorners(settings.gateMin, settings.gateMax);
							level.playSound(null, box.getCenter(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 300.0F, 0.0F);
						}
					}
				}
			}
		};
		
		private final String id;
		
		private OpenType(String id)
		{
			this.id = id;
		}
		
		@Override
		public String getSerializedName()
		{
			return this.id;
		}
		
		public abstract void tick(GateEvent.Settings settings, ServerLevel level, HuntedGame game, CompoundTag tag);
	}
}
