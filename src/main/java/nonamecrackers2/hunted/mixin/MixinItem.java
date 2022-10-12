package nonamecrackers2.hunted.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import nonamecrackers2.hunted.util.HuntedUtil;

@Mixin(Item.class)
public abstract class MixinItem implements IForgeItem
{
	@Override
	public boolean onDroppedByPlayer(ItemStack item, Player player)
	{
		if (!HuntedUtil.canDrop(item, player))
			return false;
		return IForgeItem.super.onDroppedByPlayer(item, player);
	}
}
