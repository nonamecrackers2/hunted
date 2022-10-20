package nonamecrackers2.hunted.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import nonamecrackers2.hunted.init.HuntedMenuTypes;

public class KioskMenu extends AbstractContainerMenu
{
	public KioskMenu(int id, Inventory inv)
	{
		super(HuntedMenuTypes.KIOSK.get(), id);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int p_38942_)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player)
	{
		return true;
	}
}
