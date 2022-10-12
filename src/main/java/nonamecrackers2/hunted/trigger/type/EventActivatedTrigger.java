package nonamecrackers2.hunted.trigger.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nonamecrackers2.hunted.map.event.MapEventDataManager;
import nonamecrackers2.hunted.map.event.MapEventHolder;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

public class EventActivatedTrigger extends Trigger<EventActivatedTrigger.Settings>
{
	public static final Codec<EventActivatedTrigger.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(MapEventDataManager.INSTANCE.getCodec().fieldOf("event").forGetter(EventActivatedTrigger.Settings::event)).apply(instance, EventActivatedTrigger.Settings::new);
	});
	
	public EventActivatedTrigger()
	{
		super(CODEC, Trigger.criteria().event());
	}
	
	@Override
	public boolean matches(EventActivatedTrigger.Settings settings, TriggerContext context)
	{
		if (super.matches(settings, context))
		{
			if (context.event().equals(settings.event))
				return true;
		}
		return false;
	}

	public static record Settings(MapEventHolder event) {}
}
