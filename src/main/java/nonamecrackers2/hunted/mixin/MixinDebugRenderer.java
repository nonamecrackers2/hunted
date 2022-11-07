package nonamecrackers2.hunted.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;

@Mixin(DebugRenderer.class)
public abstract class MixinDebugRenderer
{
	@Shadow
	public PathfindingRenderer pathfindingRenderer;
	
	@Inject(
		method = "render",
		at = @At("TAIL")
	)
	public void renderTail(PoseStack stack, MultiBufferSource.BufferSource buffer, double x, double y, double z, CallbackInfo ci)
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes())
			this.pathfindingRenderer.render(stack, buffer, x, y, z);
	}
}
