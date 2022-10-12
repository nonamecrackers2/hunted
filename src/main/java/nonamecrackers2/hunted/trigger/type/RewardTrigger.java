package nonamecrackers2.hunted.trigger.type;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nonamecrackers2.hunted.rewards.ButtonReward;
import nonamecrackers2.hunted.rewards.ButtonRewardsDataManager;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

public class RewardTrigger extends Trigger<RewardTrigger.Settings>
{
	public static final Codec<RewardTrigger.Settings> CODEC = RecordCodecBuilder.create(instance -> 
	{
		return instance.group(Codec.list(ButtonRewardsDataManager.INSTANCE.getCodec()).optionalFieldOf("rewards").forGetter(RewardTrigger.Settings::rewards),
				Codec.BOOL.optionalFieldOf("all_collected").forGetter(s -> Optional.of(s.allCollected)))
				.apply(instance, (rewards, allCollected) -> new RewardTrigger.Settings(rewards.map(ImmutableList::copyOf), allCollected.orElse(false)));
	});
	public static final RewardTrigger.Settings DEFAULT = new RewardTrigger.Settings(Optional.empty(), false);
	
	public RewardTrigger()
	{
		super(CODEC, Trigger.criteria().player().target().reward());
	}
	
	@Override
	public boolean matches(RewardTrigger.Settings settings, TriggerContext context)
	{
		if (super.matches(settings, context))
		{
			if (settings.rewards().isPresent())
			{
				if (!settings.allCollected())
				{
					if (settings.rewards().get().contains(context.reward()))
						return true;
				}
				else
				{
					if (context.getGame().getCollectedRewards().containsAll(settings.rewards().get()) && settings.rewards().get().contains(context.reward()))
						return true;
				}
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected RewardTrigger.Settings defaultSettings()
	{
		return DEFAULT;
	}

	public static record Settings(Optional<List<ButtonReward>> rewards, boolean allCollected) {}
}
