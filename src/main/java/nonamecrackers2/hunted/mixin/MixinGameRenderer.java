package nonamecrackers2.hunted.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import nonamecrackers2.hunted.client.init.HuntedClientCapabilities;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer
{
	@Shadow
	private Minecraft minecraft;
	@Shadow
	private float darkenWorldAmount;
	@Shadow
	private float darkenWorldAmountO;
	
	@Inject(
		method = "getDarkenWorldAmount",
		at = @At("HEAD"),
		cancellable = true
	)
	public void getDarkenWorldAmountHead(float partialTicks, CallbackInfoReturnable<Float> ci)
	{
		var manager = this.minecraft.level.getCapability(HuntedClientCapabilities.EFFECTS_MANAGER).orElse(null);
		if (manager != null)
		{
			float pulse = manager.getLightPulse(partialTicks);
			if (pulse > 0.0F)
				ci.setReturnValue(Mth.lerp(partialTicks, this.darkenWorldAmountO, this.darkenWorldAmount) + pulse);
		}
	}
}
