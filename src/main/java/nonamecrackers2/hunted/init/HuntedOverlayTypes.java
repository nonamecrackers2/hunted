package nonamecrackers2.hunted.init;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.map.overlay.CollectedRewardsOverlay;
import nonamecrackers2.hunted.map.overlay.HuntedOverlay;
import nonamecrackers2.hunted.registry.HuntedRegistries;

public class HuntedOverlayTypes
{
	public static final DeferredRegister<HuntedOverlay<?>> OVERLAYS = DeferredRegister.create(HuntedRegistries.OVERLAYS_NAME, HuntedMod.MOD_ID);
	
	public static final RegistryObject<CollectedRewardsOverlay> COLLECTED_REWARDS = OVERLAYS.register("collected_rewards", CollectedRewardsOverlay::new);
	
	public static void register(IEventBus modBus)
	{
		OVERLAYS.register(modBus);
	}
}
