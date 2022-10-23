package nonamecrackers2.hunted.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.config.HuntedConfig;
import nonamecrackers2.hunted.init.HuntedCapabilities;

public class HuntedRenderEvents
{
	@SubscribeEvent
	public static void onRenderPlayer(RenderPlayerEvent.Pre event)
	{
		if (HuntedConfig.CLIENT.horrorElements.get())
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
//
////	@SubscribeEvent
//	public static void onRenderLevelLast(RenderLevelStageEvent event)
//	{
//		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
//		{
//			Minecraft mc = Minecraft.getInstance();
////			mc.level.getCapability(HuntedClientCapabilities.GAME_INFO).ifPresent(info -> 
////			{
////				if (info.isGameRunning() && info.buttonHighlighting())
////				{
////					info.getMap().ifPresent(map -> 
////					{
//						PoseStack stack = event.getPoseStack();
//						MultiBufferSource.BufferSource source = mc.renderBuffers().bufferSource();
//						OutlineBufferSource buffer = mc.renderBuffers().outlineBufferSource();
//						buffer.setColor(255, 255, 255, 255);
//						RenderSystem.disableDepthTest();
//						VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
////						for (BlockPos blockPos : map.buttons())
////						{
//						BlockPos blockPos = mc.player.blockPosition().below();
//							stack.pushPose();
//							Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
//							Vec3 inversePos = camPos.scale(-1.0D);
//							stack.translate(inversePos.x, inversePos.y, inversePos.z);
//							stack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
//							BlockState state = mc.level.getBlockState(blockPos);
//							if (!state.isAir())
//								mc.getBlockRenderer().getModelRenderer().tesselateBlock(mc.level, mc.getBlockRenderer().getBlockModel(state), state, blockPos, stack, consumer, false, RandomSource.create(), 0, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
//								//renderShape(stack, consumer, state.getShape(mc.level, blockPos, CollisionContext.of(mc.getCameraEntity())), blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0F, 1.0F, 1.0F, 1.0F);
//							stack.popPose();
////						}
//						RenderSystem.enableDepthTest();
//						source.endBatch();
//						buffer.endOutlineBatch();
////					});
////				}
////			});
//		}
//	}
//	
//	private static void renderShape(PoseStack stack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float r, float g, float b, float a)
//	{
//		PoseStack.Pose pose = stack.last();
//		shape.forAllEdges((xl, yl, zl, xl2, yl2, zl2) ->
//		{
//			float f = (float)(xl2 - xl);
//			float f1 = (float)(yl2 - yl);
//			float f2 = (float)(zl2 - zl);
//			float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
//			f /= f3;
//			f1 /= f3;
//			f2 /= f3;
//			consumer.vertex(pose.pose(), (float)(xl + x), (float)(yl + y), (float)(zl + z)).color(r, g, b, a).uv(0, 1).normal(pose.normal(), f, f1, f2).endVertex();
//			consumer.vertex(pose.pose(), (float)(xl2 + x), (float)(yl2 + y), (float)(zl2 + z)).color(r, g, b, a).uv(1, 1).normal(pose.normal(), f, f1, f2).endVertex();
//		});
//	}
}
