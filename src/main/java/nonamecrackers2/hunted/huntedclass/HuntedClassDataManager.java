package nonamecrackers2.hunted.huntedclass;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.ability.AbilityDataManager;
import nonamecrackers2.hunted.death.DeathSequence;
import nonamecrackers2.hunted.death.DeathSequenceDataManager;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.resources.SimpleDataManager;

public class HuntedClassDataManager extends SimpleDataManager<HuntedClass>
{	
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final HuntedClassDataManager INSTANCE = new HuntedClassDataManager();
	
	private HuntedClassDataManager()
	{
		super(GSON, "hunted_classes");
	}

	@Override
	protected HuntedClass apply(Entry<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller filler) 
	{
		ResourceLocation fileName = map.getKey();
		JsonElement element = map.getValue();
		JsonObject object = GsonHelper.convertToJsonObject(element, "hunted class");
		HuntedClassType type = getTypeFromJson(object);
		if (type == null)
		{
			LOGGER.info("Skipping hunted class {} as it does not have an assigned class type", fileName);
			return null;
		}

		SoundEvent loopSound = null;
		if (object.has("loop_sound"))
		{
			String rawId = GsonHelper.getAsString(object, "loop_sound");
			loopSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(rawId));
			if (loopSound == null)
				throw new JsonSyntaxException("Unknown or unsupported sound event '" + rawId + "'");
		}
		
		boolean supportsMask = false;
		if (object.has("supports_mask"))
			supportsMask = GsonHelper.getAsBoolean(object, "supports_mask");
		
		List<Ability> abilities = getAbilities(GsonHelper.getAsJsonArray(object, "abilities"));

		Map<EquipmentSlot, Item> outfit = getOutfitFromJson(object);

		DeathSequence.ConfiguredDeathSequence<?> sequence = null;
		if (type.requiresDeathSequence())
			sequence = getConfiguredDeathSequence(object.get("death_sequence"));
		
		HuntedClass huntedClass = new HuntedClass(fileName, type, Optional.ofNullable(loopSound), supportsMask, abilities, outfit, sequence);
		return huntedClass;
	}
	
	public static HuntedClassType getTypeFromJson(JsonObject object)
	{
		String typeRaw = GsonHelper.getAsString(object, "type");
		HuntedClassType type = HuntedRegistries.HUNTED_CLASS_TYPES.get().getValue(new ResourceLocation(typeRaw));
		if (type != null)
			return type;
		else
			throw new JsonSyntaxException("Invalid or unsupported Hunted class type '" + typeRaw + "'");
	}
	
	public static Map<EquipmentSlot, Item> getOutfitFromJson(JsonObject object)
	{
		Map<EquipmentSlot, Item> items = Maps.newHashMap();
		JsonArray jsonItems = GsonHelper.getAsJsonArray(object, "outfit");
		for (JsonElement element : jsonItems)
		{
			JsonObject jsonSlot = GsonHelper.convertToJsonObject(element, "outfit piece");
			
			EquipmentSlot slot = EquipmentSlot.byName(GsonHelper.getAsString(jsonSlot, "slot"));
			String typeRaw = GsonHelper.getAsString(jsonSlot, "item");
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(typeRaw));
			if (item != null && slot != null)
				items.putIfAbsent(slot, item);
			
			if (item == null)
				throw new JsonSyntaxException("Invalid or unsupported item '" + typeRaw + "'");
		}
		return items;
	}
	
	public static List<Ability> getAbilities(JsonArray array)
	{
		ImmutableList.Builder<Ability> abilities = ImmutableList.builder();
		for (int i = 0; i < array.size(); i++)
		{
			String typeRaw = GsonHelper.convertToString(array.get(i), "ability");
			if (typeRaw.startsWith("#"))
			{
				typeRaw = typeRaw.substring(1);
				for (Map.Entry<ResourceLocation, Ability> ability : AbilityDataManager.INSTANCE.values().entrySet())
				{
					if (ability.getKey().toString().indexOf(typeRaw) == 0)
						abilities.add(ability.getValue());
				}
			}
			else
			{
				Ability ability = AbilityDataManager.INSTANCE.get(new ResourceLocation(typeRaw));
				if (ability != null)
					abilities.add(ability);
				else
					throw new JsonSyntaxException("Could not find ability '" + typeRaw + "'");
			}
		}
		return abilities.build();
	}
	
	public static DeathSequence.ConfiguredDeathSequence<?> getConfiguredDeathSequence(JsonElement element)
	{
		String rawType = GsonHelper.convertToString(element, "death_sequence");
		DeathSequence.ConfiguredDeathSequence<?> sequence = DeathSequenceDataManager.INSTANCE.get(new ResourceLocation(rawType));
		if (sequence != null)
			return sequence;
		else
			throw new JsonSyntaxException("Unknown or unsupported death sequence '" + rawType + "'");
	}
}
