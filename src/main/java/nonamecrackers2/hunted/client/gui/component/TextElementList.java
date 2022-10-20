package nonamecrackers2.hunted.client.gui.component;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class TextElementList extends ObjectSelectionList<TextElementList.Entry>
{
	private static final int INDENT_WIDTH = 10;
	private final boolean center;
	
	public TextElementList(Minecraft mc, int width, int height, int top, int bottom, boolean center)
	{
		super(mc, width, height, top, bottom, mc.font.lineHeight + 2);
		this.center = center;
	}
	
	public void line(Component text)
	{
		this.line(0, text);
	}
	
	public void line(int indent, Component text)
	{
		for (FormattedCharSequence charSequence : this.minecraft.font.split(text, this.getRowWidth() - INDENT_WIDTH * indent))
			this.addEntry(new TextElementList.Text(indent, charSequence, this.minecraft.font, this.center));
	}
	
	public void blank()
	{
		this.addEntry(new TextElementList.Text(0, null, this.minecraft.font, this.center));
	}
	
	public void image(ResourceLocation texture, int width, int height, boolean center)
	{
		int lines = height / this.itemHeight;
		this.addEntry(new TextElementList.Image(texture, width, height, center));
		for (int i = 0; i < lines; i++)
			this.blank();
	}
	
	public void clear()
	{
		this.clearEntries();
	}
	
	public abstract class Entry extends ObjectSelectionList.Entry<TextElementList.Entry> {}

	public class Text extends TextElementList.Entry
	{
		private final int indent;
		private final @Nullable FormattedCharSequence text;
		private final Font font;
		private final boolean centered;
		
		private Text(int indent, @Nullable FormattedCharSequence text, Font font, boolean centered)
		{
			this.indent = indent;
			this.text = text;
			this.font = font;
			this.centered = centered;
		}
		
		@Override
		public Component getNarration()
		{
			return CommonComponents.EMPTY;
		}

		@Override
		public void render(PoseStack stack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
		{
			if (this.text != null)
			{
				if (this.centered)
					GuiComponent.drawCenteredString(stack, this.font, this.text, left + INDENT_WIDTH * this.indent + width / 2 - this.font.width(this.text) / 2, top + height / 4, 16777215);
				else
					GuiComponent.drawString(stack, this.font, this.text, left + INDENT_WIDTH * this.indent, top + height / 4, 16777215);
			}
		}
	}
	
	public class Image extends TextElementList.Entry
	{
		private final ResourceLocation texture;
		private final int width;
		private final int height;
		private final boolean centered;
		
		private Image(ResourceLocation texture, int width, int height, boolean centered)
		{
			this.texture = texture;
			this.width = width;
			this.height = height;
			this.centered = centered;
		}
		
		@Override
		public Component getNarration()
		{
			return CommonComponents.EMPTY;
		}
		
		@Override
		public void render(PoseStack stack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
		{
			RenderSystem.setShaderTexture(0, this.texture);

			int x = left;
			if (this.centered)
				x = left + width / 2 - this.width / 2;
			TextElementList.this.blit(stack, x, top, 0, 0, this.width, this.height);
		}
	}
}
