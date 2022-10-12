package nonamecrackers2.hunted.death;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.type.PreyClassType;
import nonamecrackers2.hunted.init.HuntedPacketHandlers;
import nonamecrackers2.hunted.init.HuntedSoundEvents;
import nonamecrackers2.hunted.packet.DoJumpscarePacket;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.SoundEventHolder;

public class GenericDeathSequence extends DeathSequence<GenericDeathSequence.Settings>
{
	public static final Codec<GenericDeathSequence.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				HuntedUtil.TRANSLATABLE_COMPONENT_CODEC.fieldOf("title").forGetter(Settings::title), 
				HuntedUtil.TRANSLATABLE_COMPONENT_CODEC.fieldOf("subtitle").forGetter(Settings::subtitle),
				SoundEventHolder.CODEC.fieldOf("sound").forGetter(Settings::sound)
		).apply(instance, Settings::new);
	});
	
	public GenericDeathSequence()
	{
		super(CODEC);
	}

	@Override
	protected void runSequence(Settings settings, ServerLevel level, ServerPlayer player, HuntedClass huntedClass, HuntedGame game, CompoundTag tag)
	{
		game.removePlayer(player);
		HuntedUtil.showTitle(player, settings.title, 20, 60, 20);
		HuntedUtil.showSubtitle(player, settings.subtitle, 20, 60, 20);
		List<ServerPlayer> players = game.getPlayers();
		players.forEach(p -> p.playNotifySound(settings.sound.event(), SoundSource.PLAYERS, settings.sound.volume(), settings.sound.pitch()));
		if (players.size() > 0)
		{
			ServerPlayer toAward = players.get(player.getRandom().nextInt(players.size()));
			player.getInventory().items.forEach(item -> 
			{
				if (item.getOrCreateTag().contains("HuntedGameData"))
				{
					CompoundTag extra = item.getTagElement("HuntedGameData");
					if (extra.getBoolean("IsRewardItem"))
					{
						toAward.addItem(item);
						player.getInventory().removeItem(item);
					}
				}
			});
		}
		HuntedPacketHandlers.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new DoJumpscarePacket(HuntedSoundEvents.JUMPSCARE.get(), 50));
	}
	
	public static record Settings(MutableComponent title, MutableComponent subtitle, SoundEventHolder sound) {}
}
