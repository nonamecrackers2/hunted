package nonamecrackers2.hunted.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import nonamecrackers2.hunted.client.keybind.HuntedKeybinds;
import nonamecrackers2.hunted.config.HuntedConfig;

public enum KioskTutorialStep
{
	START 
	{
		@Override
		public void render(HuntedGameMenuScreen screen, PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height)
		{
			Minecraft mc = screen.getMinecraft();
			Font font = mc.font;
			Component text = Component.translatable("hunted.menu.tutorial.class.select").withStyle(ChatFormatting.GOLD);
			int bound = 50;
			int wordHeight = font.wordWrapHeight(text, bound);
			font.drawWordWrap(text, x - font.width(text), y + 60 - wordHeight / 2, bound, 16777215);
			GuiComponent.drawString(stack, font, Component.literal(">").withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD)), x - 60, y + 55, 16777215);
		}
	},
	CLASS_SELECT 
	{
		@Override
		public void render(HuntedGameMenuScreen screen, PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height)
		{
			Minecraft mc = screen.getMinecraft();
			Font font = mc.font;
			
			Component one = Component.translatable("hunted.menu.tutorial.class.select.1").withStyle(ChatFormatting.GOLD);
			int oneBound = HuntedGameMenuScreen.SCROLL_WINDOW_WIDTH - HuntedGameMenuScreen.WINDOW_BORDER * 2;
			int oneHeight = font.wordWrapHeight(one, oneBound);
			font.drawWordWrap(one, x + width + HuntedGameMenuScreen.WINDOW_BORDER + 20, y - HuntedGameMenuScreen.WINDOW_BORDER - oneHeight - 10, oneBound, 16777215);
			
			Component two = Component.translatable("hunted.menu.tutorial.class.select.2").withStyle(ChatFormatting.GOLD);
			int twoBound = width;
			int twoHeight = font.wordWrapHeight(two, twoBound);
			font.drawWordWrap(two, x, y - HuntedGameMenuScreen.WINDOW_BORDER - 20 - twoHeight, twoBound, 16777215);
			
			Component three = Component.translatable("hunted.menu.tutorial.class.select.3").withStyle(ChatFormatting.GOLD);
			font.drawWordWrap(three, x + width + HuntedGameMenuScreen.WINDOW_BORDER + 20, y + height + HuntedGameMenuScreen.WINDOW_BORDER + 10, oneBound, 16777215);
			
			Component text = Component.translatable("hunted.menu.tutorial.class.select.4").withStyle(ChatFormatting.GOLD);
			int bound = 50;
			int wordHeight = font.wordWrapHeight(text, bound);
			font.drawWordWrap(text, x - font.width(text) - 40, y + 88 - wordHeight / 2, bound, 16777215);
			GuiComponent.drawString(stack, font, Component.literal(">").withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD)), x - 60, y + 84, 16777215);
			
			Component keybind = Component.translatable("hunted.menu.tutorial.class.select.keybind", HuntedKeybinds.USE_ABILITY.getTranslatedKeyMessage().copy().withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.BLUE))).withStyle(ChatFormatting.GOLD);
			GuiComponent.drawString(stack, font, keybind, x, y + height + HuntedGameMenuScreen.WINDOW_BORDER + 10, 16777215);
		}
	},
	GAME_MENU 
	{
		@Override
		public void render(HuntedGameMenuScreen screen, PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height)
		{
			Minecraft mc = screen.getMinecraft();
			Font font = mc.font;
			
			Component one = Component.translatable("hunted.menu.tutorial.game.menu.1").withStyle(ChatFormatting.GOLD);
			int oneBound = 100;
			font.drawWordWrap(one, x - oneBound, y + height + 20, oneBound, 16777215);
			
			Component two = Component.translatable("hunted.menu.tutorial.game.menu.2").withStyle(ChatFormatting.GOLD);
			int twoBound = 90;
			font.drawWordWrap(two, x + width + 10 + HuntedGameMenuScreen.WINDOW_BORDER, y, twoBound, 16777215);
			
			Component three = Component.translatable("hunted.menu.tutorial.game.menu.3").withStyle(ChatFormatting.GOLD);
			int threeBound = 100;
			font.drawWordWrap(three, x + width - 20 + HuntedGameMenuScreen.WINDOW_BORDER, y + height + 20, threeBound, 16777215);
		}
	},
	FINISHED 
	{
		@Override
		public void render(HuntedGameMenuScreen screen, PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height) {}
	};
	
	public abstract void render(HuntedGameMenuScreen screen, PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height);
	
	public static void advanceIfMatches(KioskTutorialStep current)
	{
		KioskTutorialStep step = HuntedConfig.CLIENT.tutorialStep.get();
		if (current == step)
		{
			if (step.ordinal() + 1 < values().length)
				HuntedConfig.CLIENT.tutorialStep.set(values()[step.ordinal() + 1]);
		}
	}
	
	public static void renderIfMatches(KioskTutorialStep step, HuntedGameMenuScreen screen, PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height)
	{
		if (HuntedConfig.CLIENT.tutorialStep.get() == step)
			HuntedConfig.CLIENT.tutorialStep.get().render(screen, stack, mouseX, mouseY, partialTicks, x, y, width, height);
	}
}
