package nonamecrackers2.hunted.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.client.capability.HuntedClientGameInfo;
import nonamecrackers2.hunted.client.init.HuntedClientCapabilities;
import nonamecrackers2.hunted.config.HuntedConfig;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.map.HuntedMap;

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
	
	private static void renderMask(LivingEntity entity, PoseStack stack, MultiBufferSource source, float partialTicks, Camera camera)
	{
		entity.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (manager.getMask() != null)
			{
				VertexConsumer consumer = source.getBuffer(RenderType.entityCutout(manager.getMask()));
				double distanceToGround = 0.0D;
				if (!entity.onClimbable())
					distanceToGround = Math.max(0.0D, Mth.lerp(partialTicks, entity.yo, entity.getY()) - entity.level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(entity.getX()), Mth.floor(entity.getZ())));
				stack.pushPose();
				stack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
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
	
	@SubscribeEvent
	public static void onRenderFog(ViewportEvent.RenderFog event)
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && mc.player != null)
		{
			float factor = getFogFactor(mc.level, mc.player);
			if (factor > 0.0F)
			{
				event.setNearPlaneDistance(0.0F);
				event.scaleFarPlaneDistance(Math.max(0.015F, 1.0F - factor));
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onComputeFogColor(ViewportEvent.ComputeFogColor event)
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && mc.player != null)
		{
			float factor = getFogFactor(mc.level, mc.player);
			if (factor > 0.0F)
			{
				event.setRed(event.getRed() * (1.0F - factor));
				event.setBlue(event.getBlue() * (1.0F - factor));
				event.setGreen(event.getGreen() * (1.0F - factor));
			}
		}
	}
	
	private static float getFogFactor(ClientLevel level, AbstractClientPlayer player)
	{
		HuntedClientGameInfo info = level.getCapability(HuntedClientCapabilities.GAME_INFO).orElse(null);
		if (info != null)
		{
			HuntedMap map = info.getMap().orElse(null); 
			if (map != null)
			{
				PlayerClassManager manager = player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
				if (manager != null)
				{
					HuntedClass huntedClass = manager.getCurrentClass().orElse(null);
					if (huntedClass != null)
					{
						if (!huntedClass.getType().canEscape())
						{
							float factor = -1.0F;
							for (AABB exit : map.preyExits())
							{
								Vec3 center = exit.getCenter();
								Vec3 pos = player.position();
								float cFactor = Math.max(0.0F, (float)(exit.getSize() - center.distanceTo(pos) + exit.getSize())) / (float)exit.getSize();
								if (factor == -1.0F || cFactor > factor)
									factor = cFactor;
							}
							if (factor > 0.0F)
								return factor;
						}
					}
				}
			}
		}
		return 0.0F;
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
