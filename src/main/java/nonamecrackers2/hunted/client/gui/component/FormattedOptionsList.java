package nonamecrackers2.hunted.client.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class FormattedOptionsList extends OptionsList
{
	public FormattedOptionsList(Minecraft mc, int width, int height, int top, int bottom, int rowheight)
	{
		super(mc, width, height, top, bottom, rowheight);
		this.setRenderBackground(false);
		this.setRenderTopAndBottom(false);
	}

	@Override
	protected void renderItem(PoseStack stack, int mouseX, int mouseY, float partialTicks, int index, int top, int left, int width, int height)
	{
		OptionsList.Entry entry = this.getEntry(index);
		for (GuiEventListener listener : entry.children())
		{
			if (listener instanceof AbstractWidget widget)
			{
				widget.setWidth(width - 10);
				widget.setX(this.getLeft() + width / 2 - widget.getWidth() / 2);
			}
		}
		super.renderItem(stack, mouseX, mouseY, partialTicks, index, top, left, width, height);
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
	
	public void clear()
	{
		this.clearEntries();
	}
}
