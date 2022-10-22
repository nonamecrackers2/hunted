package nonamecrackers2.hunted.map.overlay;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.rewards.ButtonReward;
import nonamecrackers2.hunted.rewards.ButtonRewardsDataManager;

public class CollectedRewardsOverlay extends HuntedOverlay<CollectedRewardsOverlay.Settings>
{
	public static final Codec<CollectedRewardsOverlay.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(Codec.list(ButtonRewardsDataManager.INSTANCE.getCodec()).fieldOf("rewards").forGetter(CollectedRewardsOverlay.Settings::rewards))
				.apply(instance, rewards -> new CollectedRewardsOverlay.Settings(ImmutableList.copyOf(rewards)));
	});
	
	public CollectedRewardsOverlay()
	{
		super(CODEC);
	}
	
	@Override
	public List<Component> getText(CollectedRewardsOverlay.Settings settings, ServerLevel level, HuntedGame game)
	{
		return settings.rewards.stream().map(reward -> 
		{
			boolean flag = game.getCollectedRewards().contains(reward);
			return Component.translatable(flag ? "hunted.overlay.reward.collected" : "hunted.overlay.reward.uncollected").withStyle(Style.EMPTY.withBold(true).withColor(flag ? ChatFormatting.GREEN : ChatFormatting.RED)).append(reward.getName().copy().withStyle(Style.EMPTY.withBold(false)));
		}).collect(Collectors.toList());
	}

	public static record Settings(List<ButtonReward> rewards) {}
}
