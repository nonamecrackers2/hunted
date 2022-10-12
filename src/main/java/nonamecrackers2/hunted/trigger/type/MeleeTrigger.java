package nonamecrackers2.hunted.trigger.type;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.effect.MobEffects;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

public class MeleeTrigger extends Trigger<MeleeTrigger.Settings>
{
	public static final Codec<MeleeTrigger.Settings> CODEC = RecordCodecBuilder.create(instance ->
	{
		return instance.group(Codec.BOOL.optionalFieldOf("require_target").forGetter(s -> Optional.of(s.requiresTarget)))
				.apply(instance, b -> new MeleeTrigger.Settings(b.orElse(false)));
	});
	public static final MeleeTrigger.Settings DEFAULT = new MeleeTrigger.Settings(false);
	
	public MeleeTrigger()
	{
		super(CODEC, Trigger.criteria().player().hand());
	}
	
	@Override
	public boolean matches(MeleeTrigger.Settings settings, TriggerContext context)
	{
		if (super.matches(settings, context))
		{
			if (settings.requiresTarget && context.target().hasEffect(MobEffects.DAMAGE_RESISTANCE))
				return false;
			else
				return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public Trigger.Criteria getCriteriaAdditions(Settings settings)
	{
		var criteria = Trigger.criteria();
		if (settings.requiresTarget)
			criteria.target();
		return criteria;
	}
	
	@Override
	protected MeleeTrigger.Settings defaultSettings()
	{
		return DEFAULT;
	}

	public static record Settings(boolean requiresTarget) {}
}
