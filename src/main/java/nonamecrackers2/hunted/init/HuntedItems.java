package nonamecrackers2.hunted.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;

public class HuntedItems
{
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HuntedMod.MOD_ID);
	
	public static final RegistryObject<BlockItem> KEYHOLE = ITEMS.register("keyhole", () -> new BlockItem(HuntedBlocks.KEYHOLE.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
	public static final RegistryObject<BlockItem> KIOSK = ITEMS.register("kiosk", () -> new BlockItem(HuntedBlocks.KIOSK.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
	public static final RegistryObject<BlockItem> REWARD_BUTTON = ITEMS.register("reward_button", () -> new BlockItem(HuntedBlocks.REWARD_BUTTON.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
	public static final RegistryObject<BlockItem> BEAR_TRAP = ITEMS.register("bear_trap", () -> new BlockItem(HuntedBlocks.BEAR_TRAP.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
	
	public static final RegistryObject<Item> KEY_INVIGORATION = ITEMS.register("key_invigoration", () -> new Item(new Item.Properties().fireResistant().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));
	public static final RegistryObject<Item> KEY_MIST = ITEMS.register("key_mist", () -> new Item(new Item.Properties().fireResistant().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));
	public static final RegistryObject<Item> KEY_ORIGIN = ITEMS.register("key_origin", () -> new Item(new Item.Properties().fireResistant().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));
	public static final RegistryObject<Item> KEY_SABOTAGE = ITEMS.register("key_sabotage", () -> new Item(new Item.Properties().fireResistant().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));
	
	public static void register(IEventBus modBus)
	{
		ITEMS.register(modBus);
	}
}
