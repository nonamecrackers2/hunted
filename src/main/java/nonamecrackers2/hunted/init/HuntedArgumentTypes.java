package nonamecrackers2.hunted.init;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.commands.argument.HuntedClassArgument;
import nonamecrackers2.hunted.commands.argument.HuntedClassTypeArgument;
import nonamecrackers2.hunted.commands.argument.HuntedMapArgument;

public class HuntedArgumentTypes 
{
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, HuntedMod.MOD_ID);

	public static final RegistryObject<HuntedClassArgument.Info> HUNTED_CLASS = ARGUMENT_TYPES.register("hunted_class", () -> ArgumentTypeInfos.registerByClass(HuntedClassArgument.class, new HuntedClassArgument.Info()));
	public static final RegistryObject<SingletonArgumentInfo<HuntedMapArgument>> HUNTED_MAP = ARGUMENT_TYPES.register("hunted_map", () -> ArgumentTypeInfos.registerByClass(HuntedMapArgument.class, SingletonArgumentInfo.contextFree(HuntedMapArgument::id)));
	public static final RegistryObject<SingletonArgumentInfo<HuntedClassTypeArgument>> HUNTED_CLASS_TYPE = ARGUMENT_TYPES.register("hunted_class_type", () -> ArgumentTypeInfos.registerByClass(HuntedClassTypeArgument.class, SingletonArgumentInfo.contextFree(HuntedClassTypeArgument::type)));

	public static void register(IEventBus modBus) 
	{
		ARGUMENT_TYPES.register(modBus);
	}
}
