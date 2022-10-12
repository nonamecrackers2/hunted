package nonamecrackers2.hunted.map.event;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.resources.SimpleDataManager;
import nonamecrackers2.hunted.trigger.Trigger;

public class MapEventDataManager extends SimpleDataManager<MapEventHolder>
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final MapEventDataManager INSTANCE = new MapEventDataManager();
	
	public MapEventDataManager()
	{
		super(GSON, "map_event");
	}
	
	@Override
	protected MapEventHolder apply(Entry<ResourceLocation, JsonElement> file, ResourceManager manager, ProfilerFiller filler)
	{
		JsonElement element = file.getValue();
		JsonObject object = GsonHelper.convertToJsonObject(element, "event");
		
		Trigger.ConfiguredTrigger<?> trigger = Trigger.getTrigger(object.get("trigger"));
		List<MapEvent.ConfiguredMapEvent<?>> events = getEvents(GsonHelper.getAsJsonArray(object, "events"));
		MapEventHolder holder = new MapEventHolder(file.getKey(), trigger, events);
		trigger.verify(holder);
		
		return holder;
	}
	
	public static List<MapEvent.ConfiguredMapEvent<?>> getEvents(JsonArray array)
	{
		ImmutableList.Builder<MapEvent.ConfiguredMapEvent<?>> events = ImmutableList.builder();
		for (int i = 0; i < array.size(); i++)
		{
			JsonObject object = GsonHelper.convertToJsonObject(array.get(i), "event");
			String rawType = GsonHelper.getAsString(object, "type");
			MapEvent<?> event = HuntedRegistries.MAP_EVENTS.get().getValue(new ResourceLocation(rawType));
			if (event == null)
				throw new JsonSyntaxException("Unknown or unsupported event type '" + rawType + "'");
			events.add(event.configure(object.get("settings")));
		}
		return events.build();
	}
}
