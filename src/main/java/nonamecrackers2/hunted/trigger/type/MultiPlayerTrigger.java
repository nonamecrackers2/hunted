package nonamecrackers2.hunted.trigger.type;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.trigger.TargetCriteria;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

/**
 * A {@link Trigger} that triggers for multiple players.
 * 
 * Typical use requires the use of {@link HuntedGame#triggerForActive}, with the {@link TriggerContext#target}
 * being the player (target) in question.
 * 
 * Unlike a typical trigger, that only activates for the supplied {@link TriggerContext#player},
 * this allows for use of when other players do something. An example would be when a prey
 * is eliminated. That triggers for all players with the target player being the one who was eliminated
 */
@Deprecated
public class MultiPlayerTrigger extends Trigger<MultiPlayerTrigger.Settings>
{
	public static final Codec<MultiPlayerTrigger.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(TargetCriteria.CODEC.optionalFieldOf("target").forGetter(s -> Optional.of(s.targetCriteria))).apply(instance, b -> new MultiPlayerTrigger.Settings(b.orElse(TargetCriteria.DEFAULT)));
	});
	public static final MultiPlayerTrigger.Settings DEFAULT = new MultiPlayerTrigger.Settings(TargetCriteria.DEFAULT);
	
	public MultiPlayerTrigger(Trigger.Criteria criteria)
	{
		super(CODEC, Trigger.criteria().player().target().combine(criteria));
	}
	
	public MultiPlayerTrigger()
	{
		this(Trigger.criteria());
	}
	
	@Override
	public boolean matches(MultiPlayerTrigger.Settings settings, TriggerContext context)
	{
		if (super.matches(settings, context))
			return settings.targetCriteria().matches(context);
		else
			return false;
	}
	
	public Trigger.Criteria getCriteriaAdditions(MultiPlayerTrigger.Settings settings) 
	{
		return settings.targetCriteria().getTriggerCriteria();
	}
	
	@Override
	protected MultiPlayerTrigger.Settings defaultSettings()
	{
		return DEFAULT;
	}
	
	@Deprecated
	public static record Settings(TargetCriteria targetCriteria) {}
}
