package nonamecrackers2.hunted.ability;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.hunted.ability.type.AbilityType;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.trigger.Triggerable;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.SoundEventHolder;

public class Ability implements Triggerable
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final ResourceLocation id;
	private final List<AbilityType.ConfiguredAbilityType<?>> types;
	private final Trigger.ConfiguredTrigger<?> trigger;
	private final boolean mutable;
	private final Item abilityItem;
	private final @Nullable SoundEventHolder useSound;
	private final @Nullable SoundEventHolder resetSound;
	private final @Nullable Component text;
	
	private Component name;
	private Component lore;
	private @Nullable Component cooldownLore;
	private int cooldown = 120;
	
	private int time;
	private boolean disabled;
	
	private Ability(ResourceLocation id, List<AbilityType.ConfiguredAbilityType<?>> types, Trigger.ConfiguredTrigger<?> trigger, boolean mutable, Item abilityItem, SoundEventHolder useSound, @Nullable SoundEventHolder resetSound, @Nullable Component text, Component name, Component lore, @Nullable Component cooldownLore, int cooldown)
	{
		this.id = id;
		this.types = ImmutableList.copyOf(types);
		this.trigger = trigger;
		this.mutable = mutable;
		this.abilityItem = abilityItem;
		this.useSound = useSound;
		this.resetSound = resetSound;
		this.text = text;
		this.name = name;
		this.lore = lore;
		this.cooldownLore = cooldownLore;
		this.cooldown = cooldown;
	}
	
	public Ability(ResourceLocation id, List<AbilityType.ConfiguredAbilityType<?>> types, Trigger.ConfiguredTrigger<?> trigger, Item abilityItem, SoundEventHolder useSound, @Nullable SoundEventHolder resetSound, @Nullable Component text, Component name, Component lore, @Nullable Component cooldownLore, int cooldown)
	{
		this(id, types, trigger, false, abilityItem, useSound, resetSound, text, name, lore, cooldownLore, cooldown);
	}
	
	public ResourceLocation id()
	{
		return this.id;
	}
	
	public void toPacket(FriendlyByteBuf buffer)
	{
		buffer.writeResourceLocation(this.id);
		buffer.writeRegistryId(ForgeRegistries.ITEMS, this.abilityItem);
		buffer.writeBoolean(this.useSound != null);
		if (this.useSound != null)
			this.useSound.toPacket(buffer);
		buffer.writeBoolean(this.resetSound != null);
		if (this.resetSound != null)
			this.resetSound.toPacket(buffer);
		buffer.writeBoolean(this.text != null);
		if (this.text != null)
			buffer.writeComponent(this.text);
		buffer.writeBoolean(this.name != null);
		if (this.name != null)
			buffer.writeComponent(this.name);
		buffer.writeBoolean(this.lore != null);
		if (this.lore != null)
			buffer.writeComponent(this.lore);
		buffer.writeBoolean(this.cooldownLore != null);
		if (this.cooldownLore != null)
			buffer.writeComponent(this.cooldownLore);
		buffer.writeVarInt(this.cooldown);
	}
	
	public static Ability fromPacket(FriendlyByteBuf buffer)
	{
		ResourceLocation id = buffer.readResourceLocation();
		Item abilityItem = buffer.readRegistryId();
		SoundEventHolder useSound = null;
		if (buffer.readBoolean())
			useSound = SoundEventHolder.fromPacket(buffer);
		SoundEventHolder resetSound = null;
		if (buffer.readBoolean())
			resetSound = SoundEventHolder.fromPacket(buffer);
		Component text = null;
		if (buffer.readBoolean())
			text = buffer.readComponent();
		Component name = null;
		if (buffer.readBoolean())
			name = buffer.readComponent();
		Component lore = null;
		if (buffer.readBoolean())
			lore = buffer.readComponent();
		Component cooldownLore = null;
		if (buffer.readBoolean())
			cooldownLore = buffer.readComponent();
		int cooldown = buffer.readVarInt();
		return new Ability(id, Lists.newArrayList(), null, abilityItem, useSound, resetSound, text, name, lore, cooldownLore, cooldown);
	}
	
	public void tick(ServerLevel level, HuntedGame game, ServerPlayer player, HuntedClass huntedClass, CompoundTag tag)
	{
		if (this.isMutable())
		{
			if (!this.isDisabled())
			{
				this.assign(player);
				this.removePotentialDuplicates(player);
				if (this.time > 0)
				{
					this.time--;
					if (this.time == 0)
						this.reset(level, game, player, huntedClass, tag);
				}
				for (AbilityType.ConfiguredAbilityType<?> type : this.types)
					type.tick(level, game, player, huntedClass, tag);
			}
			else
			{
				this.clear(player);
			}
		}
		else
		{
			LOGGER.error("Cannot tick immutable ability");
		}
	}
	
	public void use(TriggerContext context, CompoundTag tag)
	{
		if (this.isMutable())
		{
			if (!this.isDisabled())
			{
				if (this.time <= 0)
				{
					boolean flag = false;
					for (var type : this.types)
					{
						if (type.use(context, tag) == AbilityType.Result.SUCCESS)
							flag = true;
					}
					if (flag)
					{
						this.time = this.cooldown;
						ItemStack stack = this.getAssociatedItem(context.player());
						HuntedUtil.setLore(stack, this.getLore());
						context.player().getCooldowns().addCooldown(stack.getItem(), this.cooldown);
						if (this.useSound != null)
							context.player().playNotifySound(this.useSound.event(), SoundSource.PLAYERS, this.useSound.volume(), this.useSound.pitch());
						if (this.text != null)
							context.player().sendSystemMessage(this.text);
					}
				}
			}
		}
		else
		{
			LOGGER.error("Cannot use immutable ability");
		}
	}
	
	@Override
	public Trigger.Criteria triggerCriteria()
	{
		Trigger.Criteria criteria = Trigger.criteria().player();
		for (var ability : this.types)
			criteria.combine(ability.triggerCriteria());
		return criteria;
	}
	
	public Trigger.ConfiguredTrigger<?> getTrigger()
	{
		return this.trigger;
	}
	
	public void assign(ServerPlayer player)
	{
		if (this.isMutable())
		{
			if (!this.abilityItem.equals(Items.AIR))
			{
				if (this.getAssociatedItem(player).isEmpty())
				{
					ItemStack stack = new ItemStack(this.abilityItem);
					CompoundTag extra = new CompoundTag();
					extra.putString("Ability", this.id.toString());
					stack.addTagElement("HuntedGameData", extra);
					stack.setHoverName(this.name);
					HuntedUtil.setLore(stack, this.getLore());
					stack.getTag().putBoolean("Unbreakable", true);
					player.getInventory().add(stack);
				}
			}
		}
		else
		{
			LOGGER.error("Cannot assign immutable ability");
		}
	}
	
	public void clear(ServerPlayer player)
	{
		if (this.isMutable())
		{
			for (ItemStack stack : player.getInventory().items)
			{
				if (stack.getOrCreateTag().contains("HuntedGameData"))
				{
					CompoundTag extra = stack.getTagElement("HuntedGameData");
					if (extra.contains("Ability"))
					{
						ResourceLocation id = new ResourceLocation(extra.getString("Ability"));
						if (id.equals(this.id()))
							player.getInventory().removeItem(stack);
					}
				}
			}
		}
	}
	
	public void reset(ServerLevel level, HuntedGame game, ServerPlayer player, HuntedClass huntedClass, CompoundTag tag)
	{
		if (this.isMutable())
		{
			this.time = 0;
			this.types.forEach(type -> type.reset(level, game, player, huntedClass, tag));
			HuntedUtil.setLore(this.getAssociatedItem(player), this.getLore());
			if (this.resetSound != null)
				player.playNotifySound(this.resetSound.event(), SoundSource.PLAYERS, this.resetSound.pitch(), this.resetSound.volume());
			player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 2.0F, 1.0F);
		}
		else
		{
			LOGGER.error("Cannot reset immutable ability");
		}
	}
	
	public int getCooldown()
	{
		return this.cooldown;
	}
	
	public int getTimer()
	{
		return this.time;
	}
	
	public Component getLore()
	{
		return this.time > 0 && this.cooldownLore != null ? this.cooldownLore : this.lore;
	}
	
	private ItemStack getAssociatedItem(Player player)
	{
		List<ItemStack> items = this.getAbilityItems(player);
		if (items.size() > 0)
			return items.get(0);
		else
			return ItemStack.EMPTY;
	}
	
	private void removePotentialDuplicates(Player player)
	{
		List<ItemStack> items = this.getAbilityItems(player);
		for (ItemStack stack : items)
		{
			if (stack.getCount() > 1)
				stack.shrink(stack.getCount() - 1);
		}
		if (items.size() > 1)
		{
			for (int i = 1; i < items.size(); i++)
				player.getInventory().removeItem(items.get(i));
		}
	}
	
	//Returns all items that are associated with this ability. There should be only one, this is used for duplication checking
	private List<ItemStack> getAbilityItems(Player player)
	{
		List<ItemStack> stacks = Lists.newArrayList();
		for (ItemStack stack : player.getInventory().items)
		{
			if (stack.getOrCreateTag().contains("HuntedGameData"))
			{
				CompoundTag extra = stack.getTagElement("HuntedGameData");
				if (extra.contains("Ability"))
				{
					ResourceLocation id = new ResourceLocation(extra.getString("Ability"));
					if (id.equals(this.id()))
						stacks.add(stack);
				}
			}
		}
		return stacks;
	}
	
	public Item getItem()
	{
		return this.abilityItem;
	}
	
	public boolean isDisabled()
	{
		return this.disabled;
	}
	
	public void setDisabled(boolean flag)
	{
		this.disabled = flag;
	}
	
	public void setCooldown(int cooldown)
	{
		this.cooldown = cooldown;
	}
	
	public void setTime(int time)
	{
		this.time = time;
	}
	
	public boolean isMutable()
	{
		return this.mutable;
	}
	
	public void save(CompoundTag tag)
	{
		if (this.isMutable())
		{
			tag.putInt("Cooldown", this.cooldown);
			tag.putInt("Time", this.time);
			tag.putBoolean("Disabled", this.disabled);
		}
		else
		{
			LOGGER.warn("Cannot save immutable ability!");
		}
	}
	
	public void read(CompoundTag tag)
	{
		if (this.isMutable())
		{
			this.cooldown = tag.getInt("Cooldown");
			this.time = tag.getInt("Time");
			this.disabled = tag.getBoolean("Disabled");
		}
		else
		{
			LOGGER.warn("Cannot read immutable ability!");
		}
	}
	
	public Ability copy()
	{
		var ability =  new Ability(this.id, this.types, this.trigger, true, this.abilityItem, this.useSound, this.resetSound, this.text, this.name, this.lore, this.cooldownLore, this.cooldown);
		ability.time = this.time;
		ability.disabled = this.disabled;
		return ability;
	}
	
	@Override
	public String toString() 
	{
		return String.format(Locale.ROOT, "%s[id='%s', types='%s', trigger='%s', item='%s', name='%s', lore='%s', coolDownLore='%s', cooldown='%s']", this.getClass().getSimpleName(), this.id.toString(), this.types.toString(), this.trigger, this.abilityItem, this.name, this.lore, this.cooldownLore, this.cooldown);
	}
}
