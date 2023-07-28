package nonamecrackers2.hunted.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.entity.HunterEntity;

public class HunterRenderer extends EntityRenderer<HunterEntity>
{
	public HunterRenderer(EntityRendererProvider.Context context)
	{
		super(context);
	}
	
	@Override
	public void render(HunterEntity entity, float p_114486_, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight)
	{
		Minecraft mc = Minecraft.getInstance();
		VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity)));
		stack.pushPose();
		stack.mulPose(Axis.YP.rotationDegrees(-mc.gameRenderer.getMainCamera().getYRot()));
		PoseStack.Pose pose = stack.last();
		consumer.vertex(pose.pose(), 1.0F, 0.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
		consumer.vertex(pose.pose(), -1.0F, 0.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
		consumer.vertex(pose.pose(), -1.0F, 2.5F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
		consumer.vertex(pose.pose(), 1.0F, 2.5F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
		stack.popPose();
		super.render(entity, p_114486_, partialTicks, stack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(HunterEntity entity)
	{
		return entity.getMask();
	}
}
