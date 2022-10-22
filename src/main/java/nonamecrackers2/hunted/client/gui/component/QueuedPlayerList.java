package nonamecrackers2.hunted.client.gui.component;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedClassTypes;
import nonamecrackers2.hunted.util.HuntedClassSelector;

public class QueuedPlayerList extends ObjectSelectionList<QueuedPlayerList.Entry>
{
	private static final int FACE_SIZE = 16;
	
	public QueuedPlayerList(Minecraft mc, int width, int height, int top, int bottom)
	{
		super(mc, width, height, top, bottom, FACE_SIZE + 8);
		this.setRenderBackground(false);
		this.setRenderTopAndBottom(false);
	}
	
	public void addPlayer(PlayerInfo info, HuntedClassSelector selector, boolean isVip)
	{
		this.addEntry(new QueuedPlayerList.Entry(info::getSkinLocation, info.getProfile().getName(), selector, isVip));
	}
	
	public void clear()
	{
		this.clearEntries();
	}
	
	@Override
	public int getRowWidth()
	{
		return this.getWidth();
	}
	
	@Override
	protected int getScrollbarPosition()
	{
		return this.x0 + this.getWidth() - 6;
	}

	public class Entry extends ObjectSelectionList.Entry<QueuedPlayerList.Entry>
	{
		private final Supplier<ResourceLocation> skinGetter;
		private final String name;
		private final HuntedClassSelector selector;
		private final boolean isVip;
		
		private Entry(Supplier<ResourceLocation> skinGetter, String name, HuntedClassSelector selector, boolean isVip)
		{
			this.skinGetter = skinGetter;
			this.name = name;
			this.selector = selector;
			this.isVip = isVip;
		}
		
		@Override
		public Component getNarration()
		{
			return Component.literal(this.name);
		}

		@Override
		public void render(PoseStack stack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
		{
			Font font = QueuedPlayerList.this.minecraft.font;
			RenderSystem.setShaderTexture(0, this.skinGetter.get());
			PlayerFaceRenderer.draw(stack, left + 4, top + (height - FACE_SIZE) / 2, FACE_SIZE);
			Component name = Component.literal(this.name);
			MutableComponent suffixWidthTester = Component.literal("...");
			Component vip = Component.literal(" VIP").withStyle(ChatFormatting.GREEN);
			if (this.isVip)
				suffixWidthTester.append(vip);
			List<FormattedCharSequence> text = font.split(name, width - 8 - FACE_SIZE - font.width(suffixWidthTester));
			if (text.size() > 0)
			{
				GuiComponent.drawString(stack, font, text.get(0), left + 8 + FACE_SIZE, top + 2, 16777215);
				MutableComponent suffix = Component.empty();
				if (text.size() > 1)
					suffix.append(Component.literal("..."));
				if (this.isVip)
					suffix.append(vip);
				GuiComponent.drawString(stack, font, suffix, left + 8 + FACE_SIZE + font.width(text.get(0)), top + 2, 16777215);
					
			}
			HuntedClass huntedClass = this.selector.getFromType(HuntedClassTypes.PREY.get());
			Component prey = huntedClass.getName();
			GuiComponent.drawString(stack, font, prey, left + 8 + FACE_SIZE, top + 4 + font.lineHeight, ChatFormatting.DARK_GRAY.getColor());
		}
	}
}
