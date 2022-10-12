package nonamecrackers2.hunted.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import nonamecrackers2.hunted.util.HuntedUtil;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer
{
	private MixinLocalPlayer(ClientLevel level, GameProfile profile, ProfilePublicKey key)
	{
		super(level, profile, key);
	}

	@Inject(
		method = "drop",
		at = @At("HEAD"),
		cancellable = true
	)
	public void dropHead(boolean flag, CallbackInfoReturnable<Boolean> ci)
	{
		if (!HuntedUtil.canDrop(this.getInventory().getSelected(), this))
			ci.setReturnValue(false);
	}
}
