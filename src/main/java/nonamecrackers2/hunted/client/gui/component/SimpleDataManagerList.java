package nonamecrackers2.hunted.client.gui.component;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.resources.SimpleDataManager;

public class SimpleDataManagerList<T> extends ObjectSelectionList<SimpleDataManagerList.Entry<T>>
{
	public SimpleDataManagerList(SimpleDataManager<T> manager, Predicate<T> filter, String path, Minecraft mc, int width, int height, int top, int bottom)
	{
		super(mc, width, height, top, bottom, mc.font.lineHeight * 2);
		for (var entry : manager.syncedValues().entrySet())
		{
			if (filter.test(entry.getValue()))
				this.addEntry(new SimpleDataManagerList.Entry<>(entry.getKey(), entry.getValue(), path, this::setSelected));
		}
	}
	
	public SimpleDataManagerList(SimpleDataManager<T> manager, String path, Minecraft mc, int width, int height, int top, int bottom)
	{
		this(manager, t -> true, path, mc, width, height, top, bottom);
	}

	public static class Entry<T> extends ObjectSelectionList.Entry<SimpleDataManagerList.Entry<T>> 
	{
		private final ResourceLocation id;
		private final T object;
		private final String path;
		private final Consumer<Entry<T>> selector;
		
		public Entry(ResourceLocation id, T object, String path, Consumer<Entry<T>> selector)
		{
			this.id = id;
			this.object = object;
			this.path = path;
			this.selector = selector;
		}
		
		public T getObject()
		{
			return this.object;
		}
		
		public ResourceLocation getId()
		{
			return this.id;
		}
		
		@Override
		public Component getNarration()
		{
			return this.getName();
		}
		
		public Component getName()
		{
			return Component.translatable(this.id.getNamespace() + "." + this.path + "." + this.id.getPath()).withStyle(Style.EMPTY.withBold(true)); 
		}

		@Override
		public void render(PoseStack stack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
		{
			Minecraft mc = Minecraft.getInstance();
			GuiComponent.drawString(stack, mc.font, this.getName(), left + 12, top + height / 4, 16777215);
		}
		
		@Override
		public boolean mouseClicked(double x, double y, int clickType)
		{
			if (clickType == 0)
			{
				this.selector.accept(this);
				return true;
			}
			else
			{
				return false;
			}
		}
	}
}
