package nonamecrackers2.hunted.client.gui.component;

import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
			RenderSystem.setShaderTexture(0, this.skinGetter.get());
			PlayerFaceRenderer.draw(stack, left + 4, top + (height - FACE_SIZE) / 2, FACE_SIZE);
			MutableComponent name = Component.literal(this.name);
			if (this.isVip)
				name.append(Component.literal(" VIP").withStyle(ChatFormatting.GREEN));
			GuiComponent.drawString(stack, QueuedPlayerList.this.minecraft.font, name, left + 8 + FACE_SIZE, top + 2, 16777215);
			HuntedClass huntedClass = this.selector.getFromType(HuntedClassTypes.PREY.get());
			Component prey = Component.translatable(huntedClass.id().getNamespace() + ".class." + huntedClass.id().getPath()).withStyle(ChatFormatting.DARK_GRAY);
			GuiComponent.drawString(stack, QueuedPlayerList.this.minecraft.font, prey, left + 8 + FACE_SIZE, top + 4 + QueuedPlayerList.this.minecraft.font.lineHeight, 16777215);
		}
	}
}
