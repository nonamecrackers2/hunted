package nonamecrackers2.hunted;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nonamecrackers2.hunted.client.capability.HuntedEffectsManager;
import nonamecrackers2.hunted.client.event.HuntedClientEvents;
import nonamecrackers2.hunted.client.event.HuntedRenderEvents;
import nonamecrackers2.hunted.client.gui.menu.KioskScreen;
import nonamecrackers2.hunted.client.init.HuntedClientCapabilities;
import nonamecrackers2.hunted.client.init.HuntedParticleProviders;
import nonamecrackers2.hunted.client.init.HuntedRenderers;
import nonamecrackers2.hunted.client.keybind.HuntedKeybinds;
import nonamecrackers2.hunted.client.overlay.HuntedOverlays;
import nonamecrackers2.hunted.client.sound.manager.HuntedSoundManager;
import nonamecrackers2.hunted.commands.HuntedCommands;
import nonamecrackers2.hunted.config.HuntedConfig;
import nonamecrackers2.hunted.event.HuntedDataEvents;
import nonamecrackers2.hunted.event.HuntedEvents;
import nonamecrackers2.hunted.game.HuntedGameManager;
import nonamecrackers2.hunted.init.AbilityTypes;
import nonamecrackers2.hunted.init.DeathSequences;
import nonamecrackers2.hunted.init.HuntedArgumentTypes;
import nonamecrackers2.hunted.init.HuntedBlockEntityTypes;
import nonamecrackers2.hunted.init.HuntedBlocks;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.HuntedClassTypes;
import nonamecrackers2.hunted.init.HuntedDataSerializers;
import nonamecrackers2.hunted.init.HuntedEntityTypes;
import nonamecrackers2.hunted.init.HuntedItems;
import nonamecrackers2.hunted.init.HuntedMemoryTypes;
import nonamecrackers2.hunted.init.HuntedMenuTypes;
import nonamecrackers2.hunted.init.HuntedOverlayTypes;
import nonamecrackers2.hunted.init.HuntedPacketHandlers;
import nonamecrackers2.hunted.init.HuntedParticleTypes;
import nonamecrackers2.hunted.init.HuntedSensorTypes;
import nonamecrackers2.hunted.init.HuntedSoundEvents;
import nonamecrackers2.hunted.init.MapEvents;
import nonamecrackers2.hunted.init.TriggerTypes;
import nonamecrackers2.hunted.registry.HuntedRegistries;

@Mod(HuntedMod.MOD_ID)
public class HuntedMod
{
	public static final String MOD_ID = "hunted";
	
	public HuntedMod()
	{
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(HuntedRegistries::registerRegistries);
		modBus.addListener(HuntedCapabilities::registerCapabilities);
		modBus.addListener(this::onCommonInit);
		modBus.addListener(this::onClientInit);
		modBus.addListener(HuntedEntityTypes::addEntityAttributes);
		modBus.addListener(HuntedItems::addToCreativeTabs);
		HuntedClassTypes.register(modBus);
		AbilityTypes.register(modBus);
		HuntedArgumentTypes.register(modBus);
		DeathSequences.register(modBus);
		MapEvents.register(modBus);
		TriggerTypes.register(modBus);
		HuntedBlocks.register(modBus);
		HuntedBlockEntityTypes.register(modBus);
		HuntedItems.register(modBus);
		HuntedOverlayTypes.register(modBus);
		HuntedSoundEvents.register(modBus);
		HuntedMenuTypes.register(modBus);
		HuntedParticleTypes.register(modBus);
		HuntedEntityTypes.register(modBus);
		HuntedDataSerializers.register(modBus);
		HuntedSensorTypes.register(modBus);
		HuntedMemoryTypes.register(modBus);
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.register(HuntedCapabilities.class);
		forgeBus.addListener(HuntedMod::registerCommands);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			modBus.addListener(HuntedClientEvents::registerBlockColors);
			modBus.addListener(HuntedKeybinds::registerKeymappings);
			modBus.addListener(HuntedOverlays::registerOverlays);
			modBus.addListener(HuntedClientCapabilities::registerCapabilities);
			modBus.register(HuntedRenderers.class);
			modBus.addListener(HuntedParticleProviders::registerProviders);
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HuntedConfig.CLIENT_SPEC);
		});
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, HuntedConfig.SERVER_SPEC);
	}
	
	public void onClientInit(final FMLClientSetupEvent event)
	{
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.register(HuntedClientEvents.class);
		forgeBus.register(HuntedRenderEvents.class);
		forgeBus.register(HuntedClientCapabilities.class);
		forgeBus.register(HuntedSoundManager.Events.class);
		forgeBus.register(HuntedEffectsManager.Events.class);
		event.enqueueWork(() -> {
			MenuScreens.register(HuntedMenuTypes.KIOSK.get(), KioskScreen::new);
		});
	}
	
	public void onCommonInit(final FMLCommonSetupEvent event)
	{
		HuntedPacketHandlers.registerPackets();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.register(HuntedEvents.class);
		forgeBus.register(HuntedGameManager.Events.class);
		forgeBus.register(HuntedDataEvents.class);
	}
	
	public static void registerCommands(RegisterCommandsEvent event)
	{
		HuntedCommands.register(event.getDispatcher());
	}
	
	public static ResourceLocation resource(String id)
	{
		return new ResourceLocation(MOD_ID, id);
	}
}
