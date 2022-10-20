package nonamecrackers2.hunted.client.gui.menu;

import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import nonamecrackers2.hunted.client.gui.HuntedGameMenuScreen;
import nonamecrackers2.hunted.menu.KioskMenu;

public class KioskScreen extends HuntedGameMenuScreen implements MenuAccess<KioskMenu>
{
	private final KioskMenu menu;
	
	public KioskScreen(KioskMenu menu, Inventory inventory, Component name)
	{
		super();
		this.menu = menu;
	}
	
	@Override
	public KioskMenu getMenu()
	{
		return this.menu;
	}
	
	@Override
	public void onClose()
	{
		this.minecraft.player.closeContainer();
		super.onClose();
	}
}
