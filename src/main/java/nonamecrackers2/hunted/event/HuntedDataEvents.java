package nonamecrackers2.hunted.event;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.ability.AbilityDataManager;
import nonamecrackers2.hunted.death.DeathSequenceDataManager;
import nonamecrackers2.hunted.huntedclass.HuntedClassDataManager;
import nonamecrackers2.hunted.init.HuntedPacketHandlers;
import nonamecrackers2.hunted.map.HuntedMapDataManager;
import nonamecrackers2.hunted.map.event.MapEventDataManager;
import nonamecrackers2.hunted.packet.AbilityDataManagerPacket;
import nonamecrackers2.hunted.packet.ButtonRewardDataManagerPacket;
import nonamecrackers2.hunted.packet.HuntedClassManagerPacket;
import nonamecrackers2.hunted.packet.HuntedMapManagerPacket;
import nonamecrackers2.hunted.packet.SimpleDataManagerPacket;
import nonamecrackers2.hunted.resources.SimpleDataManager;
import nonamecrackers2.hunted.rewards.ButtonRewardsDataManager;

public class HuntedDataEvents 
{
	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event)
	{
		event.addListener(DeathSequenceDataManager.INSTANCE);
		event.addListener(AbilityDataManager.INSTANCE);
		event.addListener(HuntedClassDataManager.INSTANCE);
		event.addListener(ButtonRewardsDataManager.INSTANCE);
		event.addListener(MapEventDataManager.INSTANCE);
		event.addListener(HuntedMapDataManager.INSTANCE);
	}
	
	@SubscribeEvent
	public static void onDatapackSync(OnDatapackSyncEvent event)
	{
		sync(event.getPlayer(), AbilityDataManager.INSTANCE, AbilityDataManagerPacket::new);
		sync(event.getPlayer(), HuntedClassDataManager.INSTANCE, HuntedClassManagerPacket::new);
		sync(event.getPlayer(), ButtonRewardsDataManager.INSTANCE, ButtonRewardDataManagerPacket::new);
		sync(event.getPlayer(), HuntedMapDataManager.INSTANCE, HuntedMapManagerPacket::new);
	}
	
	private static <T> void sync(@Nullable ServerPlayer player, SimpleDataManager<T> manager, Function<Map<ResourceLocation, T>, ? extends SimpleDataManagerPacket<T>> packet)
	{
		var values = manager.values();
		if (values != null && !values.isEmpty())
		{
			if (player != null)
				HuntedPacketHandlers.MAIN.send(PacketDistributor.PLAYER.with(() -> player), packet.apply(values));
			else
				HuntedPacketHandlers.MAIN.send(PacketDistributor.ALL.noArg(), packet.apply(values));
		}
	}
}
