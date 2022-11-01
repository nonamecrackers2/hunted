package nonamecrackers2.hunted.ability.type;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.TargetSupplier;

public class GiveItems extends AbilityType<GiveItems.Settings>
{
	public static final Codec<GiveItems.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(Codec.list(ItemStack.CODEC).fieldOf("items").forGetter(GiveItems.Settings::items),
				Codec.BOOL.optionalFieldOf("random_from_list").forGetter(s -> Optional.of(s.randomFromList)),
				Codec.BOOL.optionalFieldOf("allow_multiple").forGetter(s -> Optional.of(s.allowMultiple)),
				Codec.BOOL.optionalFieldOf("can_drop_items").forGetter(s -> Optional.of(s.canDropItems)))
				.apply(instance, (items, randomFromList, allowMultiple, canDropItems) -> new GiveItems.Settings(items, randomFromList.orElse(false), allowMultiple.orElse(true), canDropItems.orElse(true)));
	});
	
	public GiveItems()
	{
		super(CODEC);
	}

	@Override
	public AbilityType.Result use(GiveItems.Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
	{
		AbilityType.Result result = AbilityType.Result.FAIL;
		for (LivingEntity player : supplier.getPlayers(context))
		{
			AbilityType.Result current = giveItems(player, settings.items, settings.randomFromList, settings.allowMultiple, settings.canDropItems);
			if (current.isMoreSuccessful(result))
				result = current; 
		}
		return result;
//		var target = settings.target;
//		if (target.left().isPresent())
//		{
//			ServerPlayer player = target.left().get().target.apply(context);
//			return giveItems(player, settings.items, settings.randomFromList, settings.allowMultiple, settings.canDropItems);
//		}
//		else if (target.right().isPresent())
//		{
//			HuntedClassType type = target.right().get();
//			for (ServerPlayer player : context.getGame().getActive())
//			{
//				if (context.getHuntedClass(player).getType().equals(type))
//					return giveItems(player, settings.items, settings.randomFromList, settings.allowMultiple, settings.canDropItems);
//			}
//		}
//		return AbilityType.Result.FAIL;
	}
	
	private static AbilityType.Result giveItems(LivingEntity player, List<ItemStack> items, boolean randomFromList, boolean allowMultiple, boolean canDropItems)
	{
		Container inv = HuntedUtil.getInventoryFor(player);
		if (inv != null)
		{
			if (!allowMultiple ? !inv.hasAnyOf(items.stream().map(ItemStack::getItem).collect(Collectors.toSet())) : true)
			{
				List<ItemStack> stacks = Lists.newArrayList();
				if (randomFromList)
					stacks = Lists.newArrayList(items.get(player.getRandom().nextInt(items.size())).copy());
				else
					stacks = items.stream().map(ItemStack::copy).collect(Collectors.toList());
				for (ItemStack stack : stacks)
				{
					CompoundTag extra = stack.getOrCreateTagElement("HuntedGameData");
					extra.putBoolean("IsGivenItem", true);
					extra.putBoolean(HuntedUtil.IS_DROPPABLE, canDropItems);
					stack.getTag().putBoolean("Unbreakable", true);
					HuntedUtil.addItem(inv, stack);
				}
				return AbilityType.Result.SUCCESS;
			}
			else
			{
				return AbilityType.Result.FAIL;
			}
		}
		else
		{
			return AbilityType.Result.FAIL;
		}
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(GiveItems.Settings settings)
	{
		return Trigger.criteria();
	}
	
	public static record Settings(List<ItemStack> items, boolean randomFromList, boolean allowMultiple, boolean canDropItems) {}
}
