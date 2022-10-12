package nonamecrackers2.hunted.map.event;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.SoundEventHolder;
import nonamecrackers2.hunted.util.TargetSupplier;

public class TeleportEvent extends MapEvent<TeleportEvent.Settings>
{
	public static final Codec<TeleportEvent.Settings> CODEC = RecordCodecBuilder.create(instance -> 
	{
		return instance.group(TargetSupplier.CODEC.optionalFieldOf("target").forGetter(s -> Optional.of(s.supplier)),
				StringRepresentable.fromEnum(TeleportEvent.Position::values).fieldOf("position").forGetter(TeleportEvent.Settings::position),
				SoundEventHolder.CODEC.optionalFieldOf("sound").forGetter(TeleportEvent.Settings::sound),
				HuntedRegistries.HUNTED_CLASS_TYPES.get().getCodec().optionalFieldOf("class_type").forGetter(TeleportEvent.Settings::classType))
				.apply(instance, (target, position, sound, type) -> new TeleportEvent.Settings(target.orElse(TargetSupplier.DEFAULT), position, sound, type));
	});
	
	public TeleportEvent()
	{
		super(CODEC);
	}
	
	@Override
	public void activate(TeleportEvent.Settings settings, TriggerContext context, CompoundTag tag)
	{
		for (ServerPlayer player : settings.supplier().getPlayers(context))
		{
			BlockPos pos = settings.position().getPos(context.getGame(), context.getHuntedClass(player), player, settings.classType());
			player.teleportTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
			settings.sound.ifPresent(sound -> player.playNotifySound(sound.event(), SoundSource.PLAYERS, sound.volume(), sound.pitch()));
		}
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(TeleportEvent.Settings settings)
	{
		return Trigger.criteria();
	}
	
	protected static record Settings(TargetSupplier supplier, TeleportEvent.Position position, Optional<SoundEventHolder> sound, Optional<HuntedClassType> classType) {}
	
	public static enum Position implements StringRepresentable
	{
		START("start")
		{
			@Override
			public BlockPos getPos(HuntedGame game, HuntedClass playerClass, ServerPlayer player, Optional<HuntedClassType> type)
			{
				return game.getMap().startForTypes().getOrDefault(type.orElse(playerClass.getType()), game.getMap().defaultStartPos());
			}
		},
		DEFAULT_START("default_start")
		{
			@Override
			public BlockPos getPos(HuntedGame game, HuntedClass playerClass, ServerPlayer player, Optional<HuntedClassType> type)
			{
				return game.getMap().defaultStartPos();
			}
		},
		RANDOM("random")
		{
			@Override
			public BlockPos getPos(HuntedGame game, HuntedClass playerClass, ServerPlayer player, Optional<HuntedClassType> type)
			{
				List<BlockPos> positions = game.getMap().revivalPositions();
				if (positions.size() > 0)
					return positions.get(player.getRandom().nextInt(positions.size()));
				else
					return game.getMap().defaultStartPos();
			}
		};
		
		private final String id;
		
		private Position(String id)
		{
			this.id = id;
		}
		
		@Override
		public String getSerializedName()
		{
			return this.id;
		}
		
		public abstract BlockPos getPos(HuntedGame game, HuntedClass playerClass, ServerPlayer player, Optional<HuntedClassType> type);
	}
}
