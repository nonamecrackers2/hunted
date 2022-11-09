package nonamecrackers2.hunted.util;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;

public class HuntedUtil
{
	public static final String IS_DROPPABLE = "IsDroppable";
	public static final Codec<MutableComponent> TRANSLATABLE_COMPONENT_CODEC = RecordCodecBuilder.create(instance -> 
	{
		return instance.group(Codec.STRING.fieldOf("translation").forGetter(MutableComponent::getString), Style.FORMATTING_CODEC.optionalFieldOf("style").forGetter(component -> 
		{
			Style style = component.getStyle();
			if (style.isEmpty())
				return Optional.empty();
			else
				return Optional.of(style);
		}
		)).apply(instance, (string, styleOptional) ->
		{
			MutableComponent component = Component.translatable(string);
			styleOptional.ifPresent(component::withStyle);
			return component;
		});
	});
	
	public static void showTitle(ServerPlayer player, Component title, int fadeIn, int stay, int fadeOut)
	{
		//player.connection.send(new ClientboundClearTitlesPacket(true));
		player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
		player.connection.send(new ClientboundSetTitleTextPacket(title));
	}
	
	public static void showSubtitle(ServerPlayer player, Component title, int fadeIn, int stay, int fadeOut)
	{
		//player.connection.send(new ClientboundClearTitlesPacket(true));
		player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
		player.connection.send(new ClientboundSetSubtitleTextPacket(title));
	}
	
	public static void throwJSE(String text)
	{
		throw new JsonSyntaxException(text);
	}
	
	public static int count(CompoundTag tag, String id)
	{
		return count(tag, id, false);
	}
	
	public static int count(CompoundTag tag, String id, boolean inverse)
	{
		int amount = tag.getInt(id) + (inverse ? -1 : 1);
		tag.putInt(id, amount);
		return amount;
	}
	
	public static CompoundTag getOrCreateTagElement(CompoundTag tag, String id)
	{
		if (tag.contains(id))
		{
			return tag.getCompound(id);
		}
		else
		{
			CompoundTag newTag = new CompoundTag();
			tag.put(id, newTag);
			return newTag;
		}
	}
	
	public static boolean canDrop(ItemStack item, Player player)
	{
		PlayerClassManager manager = player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
		if (manager != null)
		{
			HuntedClass huntedClass = manager.getCurrentClass().orElse(null);
			if (huntedClass != null)
			{
				if (item.getOrCreateTag().contains("HuntedGameData"))
				{
					CompoundTag extra = item.getTagElement("HuntedGameData");
					if (extra.contains(HuntedUtil.IS_DROPPABLE) && !extra.getBoolean(HuntedUtil.IS_DROPPABLE))
						return false;
					if (extra.getBoolean("IsOutfitItem"))
						return false;
					for (Ability ability : huntedClass.getAllAbilities())
					{
						if (!item.isEmpty())
						{
							if (extra.contains("Ability"))
							{
								ResourceLocation id = new ResourceLocation(extra.getString("Ability"));
								if (ability.id().equals(id))
									return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	public static Component appendArgs(Component component, Object... args)
	{
		if (component.getContents() instanceof TranslatableContents contents)
		{
			MutableComponent mutable = MutableComponent.create(new TranslatableContents(contents.getKey(), ArrayUtils.addAll(contents.getArgs(), args)));
			mutable.getSiblings().addAll(component.getSiblings());
			mutable.setStyle(component.getStyle());
			return mutable;
		}
		return component;
	}
	
	public static void setLore(ItemStack stack, Component component)
	{
		CompoundTag display = stack.getOrCreateTagElement(ItemStack.TAG_DISPLAY);
		ListTag list = new ListTag();
		list.add(StringTag.valueOf(Component.Serializer.toJson(component)));
		display.put(ItemStack.TAG_LORE, list);
	}
	
	public static @Nullable Container getInventoryFor(LivingEntity entity)
	{
		if (entity instanceof InventoryCarrier carrier)
			return carrier.getInventory();
		else if (entity instanceof Player player)
			return player.getInventory();
		else
			return null;
			
	}
	
	public static ItemStack addItem(Container inv, ItemStack stack)
	{
		ItemStack copy = stack.copy();
		moveItemToOccupiedSlotsWithSameType(inv, copy);
		if (copy.isEdible())
		{
			return ItemStack.EMPTY;
		}
		else
		{
			moveItemToEmptySlots(inv, stack);
			return copy.isEmpty() ? ItemStack.EMPTY : copy;
		}
	}
	
	private static void moveItemToOccupiedSlotsWithSameType(Container inv, ItemStack stack)
	{
		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack present = inv.getItem(i);
			if (ItemStack.isSameItemSameTags(stack, present))
			{
				moveItemsBetweenStacks(inv, stack, present);
				if (stack.isEmpty())
					return;
			}
		}
	}
	
	private static void moveItemToEmptySlots(Container inv, ItemStack stack)
	{
		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack present = inv.getItem(i);
			if (present.isEmpty())
			{
				inv.setItem(i, stack.copy());
				stack.setCount(0);
				return;
			}
		}
	}
	
	private static void moveItemsBetweenStacks(Container inv, ItemStack s, ItemStack s2)
	{
		int i = Math.min(inv.getMaxStackSize(), s2.getMaxStackSize());
		int j = Math.min(s.getCount(), i - s2.getCount());
		if (j > 0)
		{
			s2.grow(j);
			s.shrink(j);
			inv.setChanged();
		}
	}
	
	public static void removeItem(Container inv, ItemStack item)
	{
		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack present = inv.getItem(i);
			if (present == item)
			{
				inv.setItem(i, ItemStack.EMPTY);
				break;
			}
		}
	}
}
