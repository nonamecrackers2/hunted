package nonamecrackers2.hunted.ability;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.hunted.ability.type.AbilityType;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.resources.SimpleDataManager;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.util.SoundEventHolder;

public class AbilityDataManager extends SimpleDataManager<Ability>
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final AbilityDataManager INSTANCE = new AbilityDataManager();
	
	private AbilityDataManager() 
	{
		super(GSON, "abilities");
	}

	@Override
	protected Ability apply(Entry<ResourceLocation, JsonElement> entry, ResourceManager manager, ProfilerFiller filler) 
	{
		ResourceLocation fileName = entry.getKey();
		JsonElement element = entry.getValue();
		
		JsonObject ability = GsonHelper.convertToJsonObject(element, "ability");

		JsonArray types = GsonHelper.getAsJsonArray(ability, "types");
		List<AbilityType.ConfiguredAbilityType<?>> abilityTypes = getConfiguredTypesFromJson(types);
		Trigger.ConfiguredTrigger<?> trigger = Trigger.getTrigger(ability.get("trigger"));
		
		for (var abilityType : abilityTypes)
			trigger.verify(abilityType);
		
		String abilityItemRaw = GsonHelper.getAsString(ability, "item");

		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(abilityItemRaw));
		if (item == null)
			throw new JsonSyntaxException("Invalid or unsupported item '" + abilityItemRaw + "'");
		
		SoundEventHolder useSound = null;
		if (ability.has("use_sound"))
			useSound = getSoundEvent(GsonHelper.getAsJsonObject(ability, "use_sound"));
		
		SoundEventHolder resetSound = null;
		if (ability.has("reset_sound"))
			resetSound = getSoundEvent(GsonHelper.getAsJsonObject(ability, "reset_sound"));
		
		Component text = Component.Serializer.fromJson(ability.get("text"));
		
		Component name = Component.Serializer.fromJson(ability.get("name"));
		if (name == null && !item.equals(Items.AIR))
			throw new JsonSyntaxException("Does not have a valid name!");

		Component lore = Component.Serializer.fromJson(ability.get("lore"));
		if (lore == null && !item.equals(Items.AIR))
			throw new JsonSyntaxException("Does not have any valid lore!");

		Component cooldownLore = Component.Serializer.fromJson(ability.get("cooldown_lore"));

		int cooldown = GsonHelper.getAsInt(ability, "cooldown");
		
		boolean disabled = false;
		if (ability.has("disabled"))
			disabled = GsonHelper.getAsBoolean(ability, "disabled");
			
		var finalAbility = new Ability(fileName, abilityTypes, trigger, item, useSound, resetSound, text, name, lore, cooldownLore, cooldown);
		
		finalAbility.setDisabled(disabled);
		
		return finalAbility;
	}
	
	public static List<AbilityType.ConfiguredAbilityType<?>> getConfiguredTypesFromJson(JsonArray types)
	{
		List<AbilityType.ConfiguredAbilityType<?>> abilityTypes = Lists.newArrayList();
		for (int j = 0; j < types.size(); j++)
		{
			JsonObject object = GsonHelper.convertToJsonObject(types.get(j), "type");
			String rawType = GsonHelper.getAsString(object, "type");
			ResourceLocation id = new ResourceLocation(rawType);
			AbilityType<?> type = HuntedRegistries.ABILITY_TYPES.get().getValue(id);
			if (type != null)
				abilityTypes.add(type.configure(object.get("settings")));
			else
				throw new JsonSyntaxException("Invalid or unsupported Ability type '" + rawType + "'");
		}
		return abilityTypes;
	}
	
	private static SoundEventHolder getSoundEvent(JsonObject object)
	{
		ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(object, "type"));
		SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(id);
		float pitch = GsonHelper.getAsFloat(object, "pitch");
		float volume = GsonHelper.getAsFloat(object, "volume");
		if (event != null)
			return new SoundEventHolder(event, pitch, volume);
		else
			throw new JsonSyntaxException("Unknown or unsupported sound event '" + id + "'");
	}
}
