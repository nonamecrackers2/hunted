package nonamecrackers2.hunted.init;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.huntedclass.type.HunterClassType;
import nonamecrackers2.hunted.huntedclass.type.PreyClassType;
import nonamecrackers2.hunted.registry.HuntedRegistries;

public class HuntedClassTypes 
{
	private static final DeferredRegister<HuntedClassType> HUNTED_CLASS_TYPES = DeferredRegister.create(HuntedRegistries.HUNTED_CLASS_TYPES_NAME, HuntedMod.MOD_ID);
	
	public static final RegistryObject<HunterClassType> HUNTER = HUNTED_CLASS_TYPES.register("hunter", () -> new HunterClassType((new HuntedClassType.Properties()).setLimit(1).setColor(0xAA0000).disableRewardCollecting().derequireDeathSequence().cannotEscape().makeDangerous()));
	public static final RegistryObject<PreyClassType> PREY = HUNTED_CLASS_TYPES.register("prey", () -> new PreyClassType((new HuntedClassType.Properties()).setColor(0x5555FF)));
	
	public static void register(IEventBus modBus)
	{
		HUNTED_CLASS_TYPES.register(modBus);
	}
}
