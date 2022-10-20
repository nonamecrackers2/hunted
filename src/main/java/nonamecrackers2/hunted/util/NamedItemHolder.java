package nonamecrackers2.hunted.util;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public record NamedItemHolder(Item item, Optional<Component> name, Optional<Component> lore)
{
	public static NamedItemHolder read(JsonElement element)
	{
		if (element instanceof JsonObject object)
		{
			String rawId = GsonHelper.getAsString(object, "item");
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(rawId));
			if (item == null)
				throw new JsonSyntaxException("Unknown or unsupported item '" + rawId + "'");
			Component name = null;
			if (object.has("name"))
				name = Component.Serializer.fromJson(object.get("name"));
			Component lore = null;
			if (object.has("lore"))
				lore = Component.Serializer.fromJson(object.get("lore"));
			return new NamedItemHolder(item, Optional.ofNullable(name), Optional.ofNullable(lore));
		}
		else
		{
			String rawId = GsonHelper.convertToString(element, "item");
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(rawId));
			if (item == null)
				throw new JsonSyntaxException("Unknown or unsupported item '" + rawId + "'");
			return new NamedItemHolder(item, Optional.empty(), Optional.empty());
		}
	}
	
	public ItemStack toStack()
	{
		ItemStack stack = new ItemStack(this.item);
		this.name.ifPresent(name -> stack.setHoverName(name));
		this.lore.ifPresent(lore -> HuntedUtil.setLore(stack, lore));
		return stack;
	}
	
	public void toPacket(FriendlyByteBuf buffer)
	{
		buffer.writeRegistryId(ForgeRegistries.ITEMS, this.item);
		buffer.writeOptional(this.name, FriendlyByteBuf::writeComponent);
		buffer.writeOptional(this.lore, FriendlyByteBuf::writeComponent);
	}
	
	public static NamedItemHolder fromPacket(FriendlyByteBuf buffer)
	{
		Item item = buffer.readRegistryId();
		Optional<Component> name = buffer.readOptional(FriendlyByteBuf::readComponent);
		Optional<Component> lore = buffer.readOptional(FriendlyByteBuf::readComponent);
		return new NamedItemHolder(item, name, lore);
	}
}
