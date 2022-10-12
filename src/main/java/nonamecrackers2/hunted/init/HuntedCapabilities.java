package nonamecrackers2.hunted.init;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.capability.HuntedClassManager;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.game.HuntedGameManager;

public class HuntedCapabilities 
{
	public static final Capability<PlayerClassManager> PLAYER_CLASS_MANAGER = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<HuntedGameManager> GAME_MANAGER = CapabilityManager.get(new CapabilityToken<>() {});
	
	public static void registerCapabilities(RegisterCapabilitiesEvent event)
	{
		event.register(PlayerClassManager.class);
		event.register(HuntedGameManager.class);
	}
	
	@SubscribeEvent
	public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		Entity entity = event.getObject();
		if (entity instanceof ServerPlayer player)
		{
			LazyOptional<HuntedClassManager> manager = LazyOptional.of(() -> new HuntedClassManager(player));
			event.addCapability(HuntedMod.resource("class_manager"), new ICapabilitySerializable<CompoundTag>() 
			{
				@Override
				public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) 
				{
					return cap == PLAYER_CLASS_MANAGER ? manager.cast() : LazyOptional.empty();
				}

				@Override
				public CompoundTag serializeNBT() 
				{
					return manager.orElse(null).write();
				}

				@Override
				public void deserializeNBT(CompoundTag nbt) 
				{
					manager.orElse(null).read(nbt);
				}
			});
		}
	}
	
	@SubscribeEvent
	public static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event)
	{
		Level level = event.getObject();
		if (level instanceof ServerLevel server)
		{
			LazyOptional<HuntedGameManager> manager = LazyOptional.of(() -> new HuntedGameManager(server));
			event.addCapability(HuntedMod.resource("class_manager"), new ICapabilitySerializable<CompoundTag>() 
			{
				@Override
				public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) 
				{
					return cap == GAME_MANAGER ? manager.cast() : LazyOptional.empty();
				}

				@Override
				public CompoundTag serializeNBT() 
				{
					return manager.orElse(null).write();
				}

				@Override
				public void deserializeNBT(CompoundTag nbt) 
				{
					manager.orElse(null).read(nbt);
				}
			});
		}
	}
}
