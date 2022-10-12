package nonamecrackers2.hunted.trigger.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

public class TimerTrigger extends Trigger<TimerTrigger.Settings>
{
	public static final Codec<TimerTrigger.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(Codec.INT.fieldOf("seconds").forGetter(TimerTrigger.Settings::seconds)).apply(instance, TimerTrigger.Settings::new);
	});
	
	public TimerTrigger()
	{
		super(CODEC, Trigger.criteria());
	}
	
	@Override
	public boolean matches(TimerTrigger.Settings settings, TriggerContext context)
	{
		if (super.matches(settings, context))
		{
			if (context.getGame().getTimeElapsed() / 20 == settings.seconds())
				return true;
		}
		return false;
	}

	public static record Settings(int seconds) {}
}
