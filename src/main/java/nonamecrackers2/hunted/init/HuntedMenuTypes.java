package nonamecrackers2.hunted.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.menu.KioskMenu;

public class HuntedMenuTypes
{
	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, HuntedMod.MOD_ID);
	
	public static final RegistryObject<MenuType<KioskMenu>> KIOSK = MENU_TYPES.register("kiosk", () -> new MenuType<>(KioskMenu::new));
	
	public static void register(IEventBus modBus)
	{
		MENU_TYPES.register(modBus);
	}
}
