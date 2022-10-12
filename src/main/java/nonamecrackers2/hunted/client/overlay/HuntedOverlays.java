package nonamecrackers2.hunted.client.overlay;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.client.init.HuntedClientCapabilities;

public class HuntedOverlays
{
	public static final ResourceLocation[] JUMPSCARES = new ResourceLocation[] {
			HuntedMod.resource("textures/jumpscare/jumpscare.png")
	};
	public static final ResourceLocation VIGNETTE = new ResourceLocation("textures/misc/vignette.png");
	
	public static void registerOverlays(RegisterGuiOverlaysEvent event)
	{
		event.registerAbove(VanillaGuiOverlay.SCOREBOARD.id(), "hunted_gui", HuntedOverlays::renderHuntedGui);
		event.registerAboveAll("hunted_jumpscare", (gui, stack, partialTicks, width, height) -> 
		{
			gui.setupOverlayRenderState(true, false);
			Minecraft mc = Minecraft.getInstance();
			mc.level.getCapability(HuntedClientCapabilities.EFFECTS_MANAGER).ifPresent(manager -> 
			{
				float time = manager.getJumpscareTime(partialTicks);
				if (time > 0 && manager.getJumpscare() != null)
				{
					float x = Mth.lerp(partialTicks, manager.getJumpscareShakeO().x, manager.getJumpscareShake().x);
					float y = Mth.lerp(partialTicks, manager.getJumpscareShakeO().y, manager.getJumpscareShake().y);
					float scale = 10.0F;
					RenderSystem.disableDepthTest();
					RenderSystem.depthMask(false);
					RenderSystem.defaultBlendFunc();
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderTexture(0, manager.getJumpscare());
					Tesselator tesselator = Tesselator.getInstance();
					BufferBuilder bufferbuilder = tesselator.getBuilder();
					bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
					bufferbuilder.vertex(x - scale, (double)height + y + scale, -90.0D).uv(0.0F, 1.0F).endVertex();
					bufferbuilder.vertex((double)width + x + scale, (double)height + y + scale, -90.0D).uv(1.0F, 1.0F).endVertex();
					bufferbuilder.vertex((double)width + x + scale, y - scale, -90.0D).uv(1.0F, 0.0F).endVertex();
					bufferbuilder.vertex(x - scale, y - scale, -90.0D).uv(0.0F, 0.0F).endVertex();
					tesselator.end();
					RenderSystem.depthMask(true);
					RenderSystem.enableDepthTest();
				}
			});
		});
		event.registerAbove(VanillaGuiOverlay.VIGNETTE.id(), "danger_vignette", (gui, stack, partialTicks, width, height) -> 
		{
			gui.setupOverlayRenderState(true, false);
			Minecraft mc = Minecraft.getInstance();
			mc.level.getCapability(HuntedClientCapabilities.EFFECTS_MANAGER).ifPresent(manager -> 
			{
				float alpha = manager.getVignetteAlpha(partialTicks);
				if (alpha > 0.0F)
				{
					RenderSystem.disableDepthTest();
					RenderSystem.depthMask(false);
					RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderColor(0.0F, alpha, alpha, 1.0F);
					RenderSystem.setShaderTexture(0, VIGNETTE);
					Tesselator tesselator = Tesselator.getInstance();
					BufferBuilder bufferbuilder = tesselator.getBuilder();
					bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
					bufferbuilder.vertex(0.0D, (double)height, -90.0D).uv(0.0F, 1.0F).endVertex();
					bufferbuilder.vertex((double)width, (double)height, -90.0D).uv(1.0F, 1.0F).endVertex();
					bufferbuilder.vertex((double)width, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
					bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
					tesselator.end();
					RenderSystem.depthMask(true);
					RenderSystem.enableDepthTest();
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.defaultBlendFunc();
				}
			});
		});
	}
	
	public static void renderHuntedGui(ForgeGui gui, PoseStack stack, float partialTicks, int width, int height)
	{
		Minecraft mc = gui.getMinecraft();
		List<Component> text = Lists.newArrayList(Component.translatable("hunted.game.title").withStyle(Style.EMPTY.withBold(true).withUnderlined(true).withColor(ChatFormatting.RED)));
		mc.level.getCapability(HuntedClientCapabilities.GAME_INFO).ifPresent(info -> {
			text.addAll(info.getOverlay());
		});
		if (text.size() > 1)
		{
			int maxTextWidth = text.stream().map(t -> gui.getFont().width(t)).sorted((t, t1) -> t1 - t).findFirst().orElse(0);
			int elementWidth = maxTextWidth + 6;
			int elementHeight = text.size() * 10 + 3;
			int x = width - elementWidth - 2;
			int y = height / 2 - 15;
			Gui.fill(stack, x, y, x + elementWidth, y + elementHeight, mc.options.getBackgroundColor(0.3F));
			for (int i = 0; i < text.size(); i++)
			{
				Component component = text.get(i);
				if (i == 0)
					Gui.drawCenteredString(stack, gui.getFont(), component, x + (elementWidth / 2), y + 3 + (i * 10), 0);
				else
					Gui.drawString(stack, gui.getFont(), component, x + 3, y + 3 + (i * 10), 0);
			}
		}
	}
	
	public static void renderTextureOverlay(ResourceLocation location, float alpha, float width, float height)
	{
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
		RenderSystem.setShaderTexture(0, location);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferbuilder.vertex(0.0D, (double)height, -90.0D).uv(0.0F, 1.0F).endVertex();
		bufferbuilder.vertex((double)width, (double)height, -90.0D).uv(1.0F, 1.0F).endVertex();
		bufferbuilder.vertex((double)width, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
		bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
		tesselator.end();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
	}
}
