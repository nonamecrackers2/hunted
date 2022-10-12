package nonamecrackers2.hunted.trigger.type;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import nonamecrackers2.hunted.block.entity.KeyholeBlockEntity;
import nonamecrackers2.hunted.rewards.ButtonReward;
import nonamecrackers2.hunted.rewards.ButtonRewardsDataManager;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

public class KeyholeTrigger extends Trigger<KeyholeTrigger.Settings>
{
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Codec<KeyholeTrigger.Settings> CODEC = RecordCodecBuilder.create(instance -> 
	{
		return instance.group(Codec.list(BlockPos.CODEC).optionalFieldOf("keyholes").forGetter(KeyholeTrigger.Settings::keyholes),
				Codec.list(ButtonRewardsDataManager.INSTANCE.getCodec()).optionalFieldOf("rewards").forGetter(KeyholeTrigger.Settings::rewards))
				.apply(instance, (keyholes, rewards) -> new KeyholeTrigger.Settings(keyholes.map(ImmutableList::copyOf), rewards.map(ImmutableList::copyOf)));
	});
	public static final KeyholeTrigger.Settings DEFAULT = new KeyholeTrigger.Settings(Optional.empty(), Optional.empty());
	
	public KeyholeTrigger()
	{
		super(CODEC, Trigger.criteria().player().hand().item().hitResult());
	}
	
	@Override
	public boolean matches(KeyholeTrigger.Settings settings, TriggerContext context)
	{
		if (super.matches(settings, context))
		{
			if (settings.keyholes().isPresent())
			{
				int rewardsInserted = 0;
				for (BlockPos pos : settings.keyholes().get())
				{
					for (ButtonReward reward : settings.rewards().orElse(ImmutableList.of()))
					{
						BlockEntity entity = context.level().getBlockEntity(pos);
						if (entity instanceof KeyholeBlockEntity keyholeEntity)
						{
							ItemStack item = keyholeEntity.getItem();
							if (!item.isEmpty() && item.getOrCreateTag().contains("HuntedGameData"))
							{
								CompoundTag extra = item.getTagElement("HuntedGameData");
								if (extra.contains("Reward"))
								{
									ResourceLocation id = new ResourceLocation(extra.getString("Reward"));
									if (reward.getId().equals(id))
									{
										rewardsInserted++;
										break;
									}
								}
							}
						}
					}
				}
				if (rewardsInserted >= settings.keyholes().get().size())
					return true;
			}
			else
			{
				List<ButtonReward> rewards = settings.rewards().orElse(ImmutableList.of());
				for (ButtonReward reward : rewards)
				{
					ItemStack stack = context.stack();
					if (stack.getOrCreateTag().contains("HuntedGameData"))
					{
						CompoundTag extra = stack.getTagElement("HuntedGameData");
						if (extra.contains("Reward"))
						{
							ResourceLocation id = new ResourceLocation(extra.getString("Reward"));
							if (reward.getId().equals(id))
								return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public Trigger.ConfiguredTrigger<KeyholeTrigger.Settings> configure(JsonElement element)
	{
		var configured = super.configure(element);
		if (configured.settings.keyholes().isPresent() && configured.settings.rewards().isEmpty())
			LOGGER.warn("This trigger is not setup properly! Keyholes are meant to use the 'rewards' list to check if all supplied keyholes contain a reward supplied by the 'rewards' list. Having just a keyhole list without the rewards will cause this trigger to not activate!");
		return configured;
	}
	
	@Override
	protected KeyholeTrigger.Settings defaultSettings()
	{
		return DEFAULT;
	}

	public static record Settings(Optional<List<BlockPos>> keyholes, Optional<List<ButtonReward>> rewards) {}
}
