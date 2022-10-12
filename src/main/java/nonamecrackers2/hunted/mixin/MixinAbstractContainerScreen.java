package nonamecrackers2.hunted.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import nonamecrackers2.hunted.client.capability.HuntedClientGameInfo;
import nonamecrackers2.hunted.client.init.HuntedClientCapabilities;
import nonamecrackers2.hunted.util.HuntedUtil;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen
{
	private MixinAbstractContainerScreen(Component component)
	{
		super(component);
	}

	@Inject(
		method = "slotClicked",
		at = @At("HEAD"),
		cancellable = true
	)
	public void slotClickedHead(Slot slot, int index, int clickAction, ClickType type, CallbackInfo ci)
	{
		if (slot != null && (!slot.getItem().isEmpty() && type == ClickType.PICKUP || type == ClickType.THROW))
		{
			ClientLevel level = this.minecraft.level;
			HuntedClientGameInfo info = level.getCapability(HuntedClientCapabilities.GAME_INFO).orElse(null);
			if (info != null)
			{
				if (info.isGameRunning())
				{
					if (!HuntedUtil.canDrop(slot.getItem(), this.minecraft.player))
						ci.cancel();
				}
			}
		}
	}
	
//	@Inject(
//		method = "keyPressed",
//		at = @At("HEAD"),
//		cancellable = true
//	)
//	public void keyPressedHead(int i, int i1, int i2, CallbackInfoReturnable<Boolean> ci)
//	{
//		ClientLevel level = this.minecraft.level;
//		if (level != null)
//		{
//			HuntedClientGameInfo info = level.getCapability(HuntedClientCapabilities.GAME_INFO).orElse(null);
//			if (info != null)
//			{
//				if (info.isGameRunning())
//				{
//					InputConstants.Key key = InputConstants.getKey(i, i1);
//					if (this.minecraft.options.keyDrop.isActiveAndMatches(key))
//						ci.setReturnValue(false);
//				}
//			}
//		}
//	}
}
