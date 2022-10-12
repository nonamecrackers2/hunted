package nonamecrackers2.hunted.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.client.capability.HuntedClientClassManager;
import nonamecrackers2.hunted.init.HuntedCapabilities;

public class HuntedRenderEvents
{
	@SubscribeEvent
	public static void onRenderPlayer(RenderPlayerEvent.Pre event)
	{
		Player player = event.getEntity();
		player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (manager.isInGame())
			{
				manager.getCurrentClass().ifPresent(huntedClass -> 
				{
					if (huntedClass.supportsMask())
					{
						event.setCanceled(true);
						Minecraft mc = Minecraft.getInstance();
						renderMask(player, event.getPoseStack(), event.getMultiBufferSource(), event.getPartialTick(), mc.gameRenderer.getMainCamera());
					}
				});
			}
		});
	}
	
	private static void renderMask(Entity entity, PoseStack stack, MultiBufferSource source, float partialTicks, Camera camera)
	{
		entity.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (manager.getMask() != null)
			{
				VertexConsumer consumer = source.getBuffer(RenderType.entityCutout(manager.getMask()));
				double distanceToGround = Math.max(0.0D, Mth.lerp(partialTicks, entity.yo, entity.getY()) - entity.level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(entity.getX()), Mth.floor(entity.getZ())));
				stack.pushPose();
				stack.mulPose(Vector3f.YP.rotationDegrees(-camera.getYRot()));
				stack.translate(0.0D, -distanceToGround, 0.0D);
				PoseStack.Pose pose = stack.last();
				consumer.vertex(pose.pose(), 1.0F, 0.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
				consumer.vertex(pose.pose(), -1.0F, 0.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
				consumer.vertex(pose.pose(), -1.0F, 2.5F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
				consumer.vertex(pose.pose(), 1.0F, 2.5F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
				stack.popPose();
			}
		});
	}
}
