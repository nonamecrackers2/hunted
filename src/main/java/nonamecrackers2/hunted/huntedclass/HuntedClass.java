package nonamecrackers2.hunted.huntedclass;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.death.DeathSequence;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.util.HuntedUtil;

public class HuntedClass
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final ResourceLocation id;
	private final Component name;
	private final Component description;
	private final HuntedClassType type;
	private final boolean mutable;
	private final Optional<SoundEvent> loopSound;
	private final boolean supportsMask;
	private final @Nullable ResourceLocation icon;
	
	private List<Ability> abilities;
	private List<Ability> passiveAbilities;
	private final Map<EquipmentSlot, Item> outfit;
	private final @Nullable DeathSequence.ConfiguredDeathSequence<?> death;
	
	private HuntedClass(ResourceLocation id, Component name, Component description, HuntedClassType type, boolean mutable, Optional<SoundEvent> loopSound, boolean supportsMask, @Nullable ResourceLocation icon, List<Ability> abilities, List<Ability> passiveAbilities, Map<EquipmentSlot, Item> outfit, DeathSequence.ConfiguredDeathSequence<?> death)
	{
		this.id = id;
		this.name = name;
		this.description = description;
		this.type = type;
		this.mutable = mutable;
		this.loopSound = loopSound;
		this.supportsMask = supportsMask;
		this.icon = icon;
		this.abilities = abilities;
		this.passiveAbilities = passiveAbilities;
		this.outfit = outfit;
		this.death = death;
	}
		
	public HuntedClass(ResourceLocation id, Component name, Component description, HuntedClassType type, Optional<SoundEvent> loopSound, boolean supportsMask, @Nullable ResourceLocation icon, List<Ability> abilities, List<Ability> passiveAbilities, Map<EquipmentSlot, Item> outfit, DeathSequence.ConfiguredDeathSequence<?> death)
	{
		this(id, name, description, type, false, loopSound, supportsMask, icon, abilities, passiveAbilities, outfit, death);
	}
	
	public void toPacket(FriendlyByteBuf buffer)
	{
		buffer.writeResourceLocation(this.id);
		buffer.writeComponent(this.name);
		buffer.writeComponent(this.description);
		buffer.writeRegistryId(HuntedRegistries.HUNTED_CLASS_TYPES.get(), this.type);
		buffer.writeVarInt(this.outfit.size());
		this.outfit.forEach((slot, item) -> 
		{
			buffer.writeEnum(slot);
			buffer.writeRegistryId(ForgeRegistries.ITEMS, item);
		});
		buffer.writeBoolean(this.loopSound.isPresent());
		this.loopSound.ifPresent(sound -> buffer.writeRegistryId(ForgeRegistries.SOUND_EVENTS, sound));
		buffer.writeBoolean(this.supportsMask);
		buffer.writeBoolean(this.icon != null);
		if (this.icon != null)
			buffer.writeResourceLocation(this.icon);
	}
	
	public static HuntedClass fromPacket(FriendlyByteBuf buffer)
	{
		ResourceLocation id = buffer.readResourceLocation();
		Component name = buffer.readComponent();
		Component description = buffer.readComponent();
		HuntedClassType type = buffer.readRegistryIdSafe(HuntedClassType.class);
		int outfitSize = buffer.readVarInt();
		Map<EquipmentSlot, Item> slots = Maps.newHashMap();
		for (int i = 0; i < outfitSize; i++)
			slots.put(buffer.readEnum(EquipmentSlot.class), buffer.readRegistryIdSafe(Item.class));
		Optional<SoundEvent> loopSound = Optional.empty();
		if (buffer.readBoolean())
			loopSound = Optional.ofNullable(buffer.readRegistryId());
		boolean supportsMask = buffer.readBoolean();
		ResourceLocation icon = null;
		if (buffer.readBoolean())
			icon = buffer.readResourceLocation();
		return new HuntedClass(id, name, description, type, loopSound, supportsMask, icon, ImmutableList.of(), ImmutableList.of(), slots, null);
	}
 	
	public ResourceLocation id()
	{
		return this.id;
	}
	
	public Component getName()
	{
		return this.name;
	}
	
	public Component getDescription()
	{
		return this.description;
	}
	
	public HuntedClassType getType()
	{
		return this.type;
	}
	
	public void tick(ServerLevel level, HuntedGame game, LivingEntity player, CompoundTag tag)
	{
		if (this.isMutable())
		{
			this.assignOutfit(player);
			this.removePotentialDuplicates(player);
			this.getAllAbilities().forEach(ability -> ability.tick(level, game, player, this, HuntedUtil.getOrCreateTagElement(tag, ability.id().toString())));
			if (this.death != null)
				this.death.tick(level, player, this, game, tag);
		}
		else
		{
			LOGGER.warn("Cannot tick an immutable HuntedClass");
		}
	}

	public HuntedClass copy()
	{
		ImmutableList.Builder<Ability> abilitiesCopy = ImmutableList.builder();
		this.abilities.forEach(ability -> abilitiesCopy.add(ability.copy()));
		ImmutableList.Builder<Ability> passivesCopy = ImmutableList.builder();
		this.passiveAbilities.forEach(ability -> passivesCopy.add(ability.copy()));
		return new HuntedClass(this.id, this.name, this.description, this.type, true, this.loopSound, this.supportsMask, this.icon, abilitiesCopy.build(), passivesCopy.build(), this.outfit, this.death);
	}
	
	public boolean isMutable()
	{
		return this.mutable;
	}
	
	public List<Ability> getAllAbilities()
	{
		List<Ability> abilities = Lists.newArrayList(this.abilities);
		abilities.addAll(this.passiveAbilities);
		if (this.isMutable())
			return abilities;
		else
			return ImmutableList.copyOf(abilities);
	}
	
	public List<Ability> getAbilities()
	{
		if (this.isMutable())
			return this.abilities;
		else
			return ImmutableList.copyOf(this.abilities);
	}
	
	public List<Ability> getPassiveAbilities()
	{
		if (this.isMutable())
			return this.passiveAbilities;
		else
			return ImmutableList.copyOf(this.passiveAbilities);
	}
	
	public Map<EquipmentSlot, Item> getOutfit()
	{
		if (this.isMutable())
			return this.outfit;
		else
			return ImmutableMap.copyOf(this.outfit);
	}
	
	public void assignOutfit(LivingEntity player)
	{
		if (this.isMutable())
		{
			this.getOutfit().forEach((slot, item) -> 
			{
				ItemStack existing = player.getItemBySlot(slot);
				if (existing.isEmpty())
				{
					ItemStack stack = new ItemStack(item);
					CompoundTag extra = new CompoundTag();
					extra.putBoolean("IsOutfitItem", true);
					stack.addTagElement("HuntedGameData", extra);
					CompoundTag tag = stack.getTag();
					tag.putBoolean("Unbreakable", true);
					player.setItemSlot(slot, stack);
				}
			});
		}
		else
		{
			LOGGER.warn("Cannot assign outfit with an immutable HuntedClass");
		}
	}
	
	public void removePotentialDuplicates(LivingEntity player)
	{
		if (this.isMutable())
		{
			Container inv = HuntedUtil.getInventoryFor(player);
			if (inv != null)
			{
				for (int i = 0; i < inv.getContainerSize(); i++)
				{
					ItemStack stack = inv.getItem(i);
					if (stack.getOrCreateTag().contains("HuntedGameData"))
					{
						CompoundTag extra = stack.getTagElement("HuntedGameData");
						if (extra.contains("IsOutfitItem"))
						{
							for (EquipmentSlot slot : EquipmentSlot.values())
							{
								if (slot.getType() == EquipmentSlot.Type.ARMOR)
								{
									ItemStack itemInSlot = player.getItemBySlot(slot);
									if (itemInSlot.equals(stack, false) && !itemInSlot.equals(stack))
									{
										HuntedUtil.removeItem(inv, stack);
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		else
		{
			LOGGER.warn("Trying to remove duplicate outfit items with an immutable HuntedClass");
		}
	}
	
	public void setAbilities(ImmutableList<Ability> abilities)
	{
		this.abilities = abilities;
	}
	
	public void setPassiveAbilities(ImmutableList<Ability> abilities)
	{
		this.passiveAbilities = abilities;
	}
	
	public Optional<SoundEvent> getLoopSound()
	{
		return this.loopSound;
	}
	
	public boolean supportsMask()
	{
		return this.supportsMask;
	}
	
	public @Nullable ResourceLocation getIcon()
	{
		return this.icon;
	}
	
	public void save(CompoundTag tag)
	{
		if (this.isMutable())
		{
			tag.put("Abilities", saveAbilityList(this.abilities));
			tag.put("PassiveAbilities", saveAbilityList(this.passiveAbilities));
		}
		else
		{
			LOGGER.warn("Cannot save immutable ability!");
		}
	}
	
	private static ListTag saveAbilityList(List<Ability> abilities)
	{
		ListTag list = new ListTag();
		abilities.forEach(ability -> 
		{
			CompoundTag abilityTag = new CompoundTag();
			ability.save(abilityTag);
			list.add(abilityTag);
		});
		return list;
	}
	
	public void read(CompoundTag tag)
	{
		if (this.isMutable())
		{
			readAbilityList(this.abilities, tag.getList("Abilities", 10));
			readAbilityList(this.passiveAbilities, tag.getList("PassiveAbilities", 10));
		}
		else
		{
			LOGGER.warn("Cannot read immutable ability!");
		}
	}
	
	private static void readAbilityList(List<Ability> abilities, ListTag list)
	{
		if (abilities.size() == list.size())
		{
			for (int i = 0; i < list.size(); i++)
			{
				CompoundTag abilityTag = list.getCompound(i);
				abilities.get(i).read(abilityTag);
			}
		}
		else
		{
			LOGGER.error("Saved ability data and current abilities do not match!");
		}
	}
	
	@Override
	public String toString() 
	{
		return String.format(Locale.ROOT, "%s[id='%s', type='%s', abilities='%s', outfit='%s', death_sequence='%s']", this.getClass().getSimpleName(), this.id.toString(), this.getType().toString(), this.abilities, this.outfit, this.death);
	}
	
	public String getTypeTranslation()
	{
		ResourceLocation id = HuntedRegistries.HUNTED_CLASS_TYPES.get().getKey(this.type);
		return id.getNamespace() + ".class_type." + id.getPath();
	}
	
	public void runDeathSequence(LivingEntity player, HuntedGame game, CompoundTag tag)
	{
		if (this.death != null)
			this.death.runSequence((ServerLevel)player.getLevel(), player, this, game, tag);
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof HuntedClass huntedClass)
		{
			if (!this.isMutable() && !huntedClass.isMutable())
				return this.id().equals(huntedClass.id());
			else
				return super.equals(obj);
		}
		else
		{
			return false;
		}
	}
}
