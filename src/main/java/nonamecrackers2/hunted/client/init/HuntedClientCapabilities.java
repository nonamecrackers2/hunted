package nonamecrackers2.hunted.client.init;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.client.capability.HuntedClientClassManager;
import nonamecrackers2.hunted.client.capability.HuntedClientGameInfo;
import nonamecrackers2.hunted.client.capability.HuntedEffectsManager;
import nonamecrackers2.hunted.client.sound.manager.HuntedSoundManager;
import nonamecrackers2.hunted.init.HuntedCapabilities;

public class HuntedClientCapabilities
{
	public static final Capability<HuntedClientGameInfo> GAME_INFO = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<HuntedSoundManager> SOUND_MANAGER = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<HuntedEffectsManager> EFFECTS_MANAGER = CapabilityManager.get(new CapabilityToken<>() {});
	
	public static void registerCapabilities(RegisterCapabilitiesEvent event)
	{
		event.register(HuntedClientGameInfo.class);
		event.register(HuntedSoundManager.class);
		event.register(HuntedEffectsManager.class);
	}
	
	@SubscribeEvent
	public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		Entity entity = event.getObject();
		if (entity instanceof AbstractClientPlayer player)
		{
			LazyOptional<HuntedClientClassManager> manager = LazyOptional.of(() -> new HuntedClientClassManager(player));
			event.addCapability(HuntedMod.resource("client_game_manager"), new ICapabilityProvider() 
			{
				@Override
				public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) 
				{
					return cap == HuntedCapabilities.PLAYER_CLASS_MANAGER ? manager.cast() : LazyOptional.empty();
				}
			});
		}
	}
	
	@SubscribeEvent
	public static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event)
	{
		LazyOptional<HuntedClientGameInfo> info = LazyOptional.of(HuntedClientGameInfo::new);
		event.addCapability(HuntedMod.resource("game_info"), new ICapabilityProvider()
		{
			@Override
			public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
			{
				return cap == GAME_INFO ? info.cast() : LazyOptional.empty();
			}
		});
		LazyOptional<HuntedSoundManager> soundManager = LazyOptional.of(() -> new HuntedSoundManager(Minecraft.getInstance()));
		event.addCapability(HuntedMod.resource("sound_manager"), new ICapabilityProvider()
		{
			@Override
			public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
			{
				return cap == SOUND_MANAGER ? soundManager.cast() : LazyOptional.empty();
			}
		});
		LazyOptional<HuntedEffectsManager> effectsManager = LazyOptional.of(() -> new HuntedEffectsManager(Minecraft.getInstance()));
		event.addCapability(HuntedMod.resource("effects_manager"), new ICapabilityProvider()
		{
			@Override
			public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
			{
				return cap == EFFECTS_MANAGER ? effectsManager.cast() : LazyOptional.empty();
			}
		});
	}
}
