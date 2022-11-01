package nonamecrackers2.hunted.death;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
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
	protected void runSequence(Settings settings, ServerLevel level, LivingEntity player, HuntedClass huntedClass, HuntedGame game, CompoundTag tag)
	{
		game.removePlayer(player);
		List<LivingEntity> players = game.getPlayers();
		players.forEach(p -> 
		{
			if (p instanceof ServerPlayer sp)
				sp.playNotifySound(settings.sound.event(), SoundSource.PLAYERS, settings.sound.volume(), settings.sound.pitch());
		});
		List<LivingEntity> active = game.getActiveBy(PreyClassType.class);
		List<LivingEntity> awardable = active.stream().filter(p -> HuntedUtil.getInventoryFor(p) != null).toList();
		if (awardable.size() > 0)
		{
			LivingEntity toAward = awardable.get(player.getRandom().nextInt(active.size()));
			Container inv = HuntedUtil.getInventoryFor(player);
			if (inv != null)
			{
				for (int i = 0; i < inv.getContainerSize(); i++)
				{
					ItemStack item = inv.getItem(i);
					if (item.getOrCreateTag().contains("HuntedGameData"))
					{
						CompoundTag extra = item.getTagElement("HuntedGameData");
						if (extra.getBoolean("IsRewardItem"))
						{
							HuntedUtil.addItem(HuntedUtil.getInventoryFor(toAward), item);
							HuntedUtil.removeItem(inv, item);
						}
					}
				}
			}
		}
		if (player instanceof ServerPlayer serverPlayer)
		{
			HuntedUtil.showTitle(serverPlayer, settings.title, 20, 60, 20);
			HuntedUtil.showSubtitle(serverPlayer, settings.subtitle, 20, 60, 20);
			HuntedPacketHandlers.MAIN.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new DoJumpscarePacket(HuntedSoundEvents.JUMPSCARE.get(), 50));
		}
	}
	
	public static record Settings(MutableComponent title, MutableComponent subtitle, SoundEventHolder sound) {}
}
