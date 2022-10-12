package nonamecrackers2.hunted.death;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.game.HuntedGameManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.TriggerTypes;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.SoundEventHolder;

public class ReviveDeathSequence extends DeathSequence<ReviveDeathSequence.Settings>
{
	public static final Logger LOGGER = LogManager.getLogger();
	public static final Codec<ReviveDeathSequence.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				ResourceLocation.CODEC.fieldOf("after").forGetter(ReviveDeathSequence.Settings::after),
				Codec.INT.fieldOf("revival_times").forGetter(ReviveDeathSequence.Settings::revivalTimes),
				Codec.INT.fieldOf("revive_delay").forGetter(ReviveDeathSequence.Settings::reviveDelay),
				HuntedUtil.TRANSLATABLE_COMPONENT_CODEC.fieldOf("title").forGetter(Settings::title), 
				HuntedUtil.TRANSLATABLE_COMPONENT_CODEC.fieldOf("subtitle").forGetter(Settings::subtitle),
				SoundEventHolder.CODEC.fieldOf("sound").forGetter(ReviveDeathSequence.Settings::sound),
				SoundEventHolder.CODEC.fieldOf("revive_sound").forGetter(ReviveDeathSequence.Settings::reviveSound)
		).apply(instance, ReviveDeathSequence.Settings::new);
	});
	public static final String REVIVAL_TIMES = "RevivalTimes";
	public static final String DELAY = "ReviveDelay";
	
	public ReviveDeathSequence()
	{
		super(CODEC);
	}

	@Override
	protected void runSequence(ReviveDeathSequence.Settings settings, ServerLevel level, ServerPlayer player, HuntedClass huntedClass, HuntedGame game, CompoundTag tag)
	{
		if (tag.getInt(REVIVAL_TIMES) < settings.revivalTimes)
		{
			HuntedUtil.showTitle(player, settings.title, 20, 60, 20);
			HuntedUtil.showSubtitle(player, settings.subtitle, 20, 60, 20);
			List<ServerPlayer> players = game.getPlayers();
			players.forEach(p -> p.playNotifySound(settings.sound.event(), SoundSource.PLAYERS, settings.sound.volume(), settings.sound.pitch()));
			player.setGameMode(GameType.SPECTATOR);
			HuntedUtil.count(tag, REVIVAL_TIMES);
			tag.putInt(DELAY, settings.reviveDelay);
		}
		else
		{
			DeathSequence.ConfiguredDeathSequence<?> sequence = DeathSequenceDataManager.INSTANCE.get(settings.after);
			if (sequence != null)
				sequence.runSequence(level, player, huntedClass, game, tag);
			else
				LOGGER.error("Could not run death sequence, either invalid or unsupported. '{}'", settings.after.toString());
		}
	}
	
	@Override
	protected void tick(ReviveDeathSequence.Settings settings, ServerLevel level, ServerPlayer player, HuntedClass huntedClass, HuntedGame game, CompoundTag tag)
	{
		if (tag.getInt(REVIVAL_TIMES) >= settings.revivalTimes)
		{
			if (tag.getInt(DELAY) > 0)
			{
				HuntedUtil.count(tag, DELAY, true);
				if (tag.getInt(DELAY) == 0)
				{
					BlockPos pos = game.getMap().defaultStartPos();
					var positions = game.getMap().revivalPositions();
					if (positions.size() > 0)
						pos = positions.get(player.getRandom().nextInt(positions.size()));
					player.teleportTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
					player.setGameMode(HuntedGameManager.DEFAULT_GAME_MODE);
					game.triggerForActive(TriggerTypes.REVIVED.get(), TriggerContext.builder().target(player));
				}
			}
		}
	}
	
	public static record Settings(ResourceLocation after, int revivalTimes, int reviveDelay, MutableComponent title, MutableComponent subtitle, SoundEventHolder sound, SoundEventHolder reviveSound) {}
}
