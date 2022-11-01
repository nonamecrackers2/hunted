package nonamecrackers2.hunted.map;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.map.event.MapEventDataManager;
import nonamecrackers2.hunted.map.event.MapEventHolder;
import nonamecrackers2.hunted.map.overlay.HuntedOverlay;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.resources.SimpleDataManager;
import nonamecrackers2.hunted.rewards.ButtonReward;
import nonamecrackers2.hunted.rewards.ButtonRewardsDataManager;
import nonamecrackers2.hunted.util.HuntedUtil;

public class HuntedMapDataManager extends SimpleDataManager<HuntedMap>
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final HuntedMapDataManager INSTANCE = new HuntedMapDataManager();
	
	private HuntedMapDataManager()
	{
		super(GSON, "map");
	}

	@Override
	protected HuntedMap apply(Entry<ResourceLocation, JsonElement> file, ResourceManager manager, ProfilerFiller filler)
	{
		JsonElement element = file.getValue();
		ResourceLocation id = file.getKey();
		
		JsonObject object = GsonHelper.convertToJsonObject(element, "hunted_map");
		
		Component name = Component.Serializer.fromJson(object.get("name"));
		Map<HuntedClassType, BlockPos> startPositions = getStartPositions(GsonHelper.getAsJsonArray(object, "start_positions"));
		BlockPos defaultStartPos = BlockPos.CODEC.parse(JsonOps.INSTANCE, object.get("default_start_pos")).resultOrPartial(HuntedUtil::throwJSE).get();
		List<BlockPos> buttons = getBlockPosList(GsonHelper.getAsJsonArray(object, "buttons"));
		List<ButtonReward> rewards = getButtonRewards(GsonHelper.getAsJsonArray(object, "rewards"));
		if (rewards.size() != buttons.size())
		{
			LOGGER.warn("Skipping map {}: There must be a single reward for each button!", id);
			return null;
		}
		AABB boundary = getAabb(GsonHelper.getAsJsonObject(object, "boundary"));
		List<AABB> preyExits = getPreyExits(object.get("prey_exit"));
		List<MapEventHolder> events = ImmutableList.of();
		if (object.has("events"))
			events = getEvents(GsonHelper.getAsJsonArray(object, "events"));
		for (ButtonReward reward : rewards)
			events.addAll(reward.getEvents());
		List<BlockPos> keyholes = ImmutableList.of();
		if (object.has("keyholes"))
			keyholes = getBlockPosList(GsonHelper.getAsJsonArray(object, "keyholes"));
		Optional<HuntedOverlay.ConfiguredOverlay<?>> overlay = Optional.empty();
		if (object.has("overlay"))
			overlay = Optional.of(getOverlay(GsonHelper.getAsJsonObject(object, "overlay")));
		List<BlockPos> revivalPositions = getBlockPosList(GsonHelper.getAsJsonArray(object, "revival_positions"));
		int buttonPressingDelay = 0;
		if (object.has("button_pressing_delay"))
			buttonPressingDelay = GsonHelper.getAsInt(object, "button_pressing_delay");
		AmbienceSettings ambience = null;
		if (object.has("ambience"))
			ambience = AmbienceSettings.fromJson(GsonHelper.getAsJsonObject(object, "ambience"));
		MapNavigation nav = null;
		if (object.has("navigation"))
			nav = MapNavigation.fromJson(GsonHelper.getAsJsonObject(object, "navigation"));
		return new HuntedMap(id, name, startPositions, defaultStartPos, buttons, rewards, boundary, preyExits, events, keyholes, overlay, revivalPositions, buttonPressingDelay, Optional.ofNullable(ambience), Optional.ofNullable(nav));
	}
	
	private static Map<HuntedClassType, BlockPos> getStartPositions(JsonArray array)
	{
		ImmutableMap.Builder<HuntedClassType, BlockPos> startPositions = ImmutableMap.builder();
		for (int i = 0; i < array.size(); i++)
		{
			JsonObject object = GsonHelper.convertToJsonObject(array.get(i), "start_pos");
			ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(object, "type"));
			HuntedClassType type = HuntedRegistries.HUNTED_CLASS_TYPES.get().getValue(id);
			BlockPos pos = BlockPos.CODEC.parse(JsonOps.INSTANCE, object.get("pos")).resultOrPartial(HuntedUtil::throwJSE).get();
			if (type != null)
				startPositions.put(type, pos);
			else
				throw new JsonSyntaxException("Unknown or unsupported hunted class type '" + id + "'");
		}
		return startPositions.build();
	}
	
	private static List<BlockPos> getBlockPosList(JsonArray array)
	{
		ImmutableList.Builder<BlockPos> buttonPositions = ImmutableList.builder();
		for (int i = 0; i < array.size(); i++)
			buttonPositions.add(BlockPos.CODEC.parse(JsonOps.INSTANCE, array.get(i)).resultOrPartial(HuntedUtil::throwJSE).get());
		return buttonPositions.build();
	}
	
	private static List<ButtonReward> getButtonRewards(JsonArray array)
	{
		ImmutableList.Builder<ButtonReward> rewards = ImmutableList.builder();
		for (int i = 0; i < array.size(); i++)
		{
			ResourceLocation id = new ResourceLocation(GsonHelper.convertToString(array.get(i), "reward"));
			ButtonReward reward = ButtonRewardsDataManager.INSTANCE.get(id);
			if (reward != null)
				rewards.add(reward);
			else
				throw new JsonSyntaxException("Unknown or unsupported reward '" + id + "'");
		}
		return rewards.build();
	}
	
	private static AABB getAabb(JsonObject object)
	{
		BlockPos min = BlockPos.CODEC.parse(JsonOps.INSTANCE, object.get("min")).resultOrPartial(HuntedUtil::throwJSE).get();
		BlockPos max = BlockPos.CODEC.parse(JsonOps.INSTANCE, object.get("max")).resultOrPartial(HuntedUtil::throwJSE).get();
		return new AABB(min, max);
	}
	
	private static List<MapEventHolder> getEvents(JsonArray array)
	{
		List<MapEventHolder> events = Lists.newArrayList();
		for (int i = 0; i < array.size(); i++)
		{
			String rawId = GsonHelper.convertToString(array.get(i), "event");
			if (rawId.startsWith("#"))
			{
				rawId = rawId.substring(1);
				for (Map.Entry<ResourceLocation, MapEventHolder> event : MapEventDataManager.INSTANCE.values().entrySet())
				{
					if (event.getKey().toString().indexOf(rawId) == 0)
						events.add(event.getValue());
				}
			}
			else
			{
				ResourceLocation id = new ResourceLocation(rawId);
				MapEventHolder event = MapEventDataManager.INSTANCE.get(id);
				if (event != null)
					events.add(event);
				else
					throw new JsonSyntaxException("Unknown event '" + id + "'");
			}
		}
		return events;
	}
	
	private static HuntedOverlay.ConfiguredOverlay<?> getOverlay(JsonObject object)
	{
		ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(object, "type"));
		HuntedOverlay<?> overlay = HuntedRegistries.OVERLAYS.get().getValue(id);
		if (overlay == null)
			throw new JsonSyntaxException("Unknown or unsupported overlay type '" + id + "'");
		return overlay.configure(object.get("settings"));
	}
	
	private static List<AABB> getPreyExits(JsonElement element)
	{
		ImmutableList.Builder<AABB> exits = ImmutableList.builder();
		if (element instanceof JsonObject object)
		{
			exits.add(getAabb(object));
		}
		else if (element instanceof JsonArray array)
		{
			for (int i = 0; i < array.size(); i++)
				exits.add(getAabb(GsonHelper.convertToJsonObject(array.get(i), "exit")));
		}
		return exits.build();
	}
}
