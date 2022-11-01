package nonamecrackers2.hunted.init;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;

public class HuntedDataSerializers
{
	private static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, HuntedMod.MOD_ID);
	
	public static final RegistryObject<EntityDataSerializer<ResourceLocation>> RESOURCE_LOCATION = SERIALIZERS.register("resource_location", () -> EntityDataSerializer.simple(FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::readResourceLocation));
	
	public static void register(IEventBus modBus)
	{
		SERIALIZERS.register(modBus);
	}
}
