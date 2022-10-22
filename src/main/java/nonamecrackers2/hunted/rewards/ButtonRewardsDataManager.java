package nonamecrackers2.hunted.rewards;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.hunted.resources.SimpleDataManager;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.MobEffectHolder;
import nonamecrackers2.hunted.util.NamedItemHolder;
import nonamecrackers2.hunted.util.TargetSupplier;

public class ButtonRewardsDataManager extends SimpleDataManager<ButtonReward>
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final ButtonRewardsDataManager INSTANCE = new ButtonRewardsDataManager();
	
	private ButtonRewardsDataManager()
	{
		super(GSON, "button_rewards");
	}
	
	@Override
	protected ButtonReward apply(Entry<ResourceLocation, JsonElement> value, ResourceManager manager, ProfilerFiller filler) 
	{
		JsonElement element = value.getValue();
		JsonObject object = GsonHelper.convertToJsonObject(element, "reward");
		Component name = Component.Serializer.fromJson(object.get("name"));
		if (!object.has("message"))
			throw new JsonSyntaxException("Must include a reward message!");
		Component globalMessage = null;
		if (object.has("global_message"))
			globalMessage = Component.Serializer.fromJson(object.get("global_message"));
		TargetSupplier globalMessageSupplier = new TargetSupplier(TargetSupplier.Type.ALL, Optional.empty(), Optional.empty(), Optional.empty());
		List<Component> messages = getMessages(object.get("message"));
		boolean randomMessage = false;
		if (object.has("random_message"))
			randomMessage = GsonHelper.getAsBoolean(object, "random_message");
		if (object.has("global_message_target"))
			globalMessageSupplier = TargetSupplier.CODEC.parse(JsonOps.INSTANCE, object.get("global_message_target")).resultOrPartial(HuntedUtil::throwJSE).get();
		ResourceLocation soundId = new ResourceLocation(GsonHelper.getAsString(object, "sound"));
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(soundId);
		if (sound == null)
			throw new JsonSyntaxException("Unknown or unsupported sound event '" + soundId + "'");
		float pitch = GsonHelper.getAsFloat(object, "pitch");
		List<NamedItemHolder> items = getItemList(GsonHelper.getAsJsonArray(object, "items"));
		List<MobEffectHolder> effects = getMobEffects(GsonHelper.getAsJsonArray(object, "effects"));
		TargetSupplier supplier = TargetSupplier.DEFAULT;
		if (object.has("effect_target"))
			supplier = TargetSupplier.CODEC.parse(JsonOps.INSTANCE, object.get("effect_target")).resultOrPartial(HuntedUtil::throwJSE).get();
		List<String> events = Lists.newArrayList();
		if (object.has("events"))
			events = getEvents(GsonHelper.getAsJsonArray(object, "events"));
		return new ButtonReward(value.getKey(), name, globalMessage, globalMessageSupplier, messages, randomMessage, sound, pitch, items, effects, supplier, events);
	}
	
	private static List<NamedItemHolder> getItemList(JsonArray array)
	{
		List<NamedItemHolder> items = Lists.newArrayList();
		for (int i = 0; i < array.size(); i++)
			items.add(NamedItemHolder.read(array.get(i)));
		return items;
	}
	
	private static List<MobEffectHolder> getMobEffects(JsonArray array)
	{
		List<MobEffectHolder> effects = Lists.newArrayList();
		for (int i = 0; i < array.size(); i++)
			effects.add(MobEffectHolder.CODEC.parse(JsonOps.INSTANCE, array.get(i)).resultOrPartial(HuntedUtil::throwJSE).get());
		return effects;
	}
	
	private static List<String> getEvents(JsonArray array)
	{
		ImmutableList.Builder<String> list = ImmutableList.builder();
		for (int i = 0; i < array.size(); i++)
		{
			String rawId = GsonHelper.convertToString(array.get(i), "event");
			list.add(rawId);
		}
		return list.build();
	}
	
	private static List<Component> getMessages(JsonElement element)
	{
		ImmutableList.Builder<Component> list = ImmutableList.builder();
		if (element instanceof JsonArray array)
		{
			for (int i = 0; i < array.size(); i++)
				list.add(Component.Serializer.fromJson(array.get(i)));
		}
		else
		{
			list.add(Component.Serializer.fromJson(element));
		}
		return list.build();
	}
}
