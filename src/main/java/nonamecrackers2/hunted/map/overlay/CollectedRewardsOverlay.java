package nonamecrackers2.hunted.map.overlay;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.rewards.ButtonReward;
import nonamecrackers2.hunted.rewards.ButtonRewardsDataManager;

public class CollectedRewardsOverlay extends HuntedOverlay<CollectedRewardsOverlay.Settings>
{
	public static final Codec<CollectedRewardsOverlay.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(Codec.unboundedMap(Codec.STRING, Style.FORMATTING_CODEC).fieldOf("rewards")
				.forGetter(s -> s.rewards.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getId().toString(), Map.Entry::getValue))))
				.apply(instance, l -> new CollectedRewardsOverlay.Settings(l.entrySet().stream().collect(Collectors.toUnmodifiableMap(e -> Objects.requireNonNull(ButtonRewardsDataManager.INSTANCE.get(new ResourceLocation(e.getKey())), "Unknown or unsupported reward type '" + e.getKey() + "'"), Map.Entry::getValue))));
	});
	
	public CollectedRewardsOverlay()
	{
		super(CODEC);
	}
	
	@Override
	public List<Component> getText(CollectedRewardsOverlay.Settings settings, ServerLevel level, HuntedGame game)
	{
		return settings.rewards.entrySet().stream().map(entry -> 
		{
			ResourceLocation id = entry.getKey().getId();
			Component component = Component.translatable(id.getNamespace() + ".reward." + id.getPath()).withStyle(entry.getValue());
			boolean flag = game.getCollectedRewards().contains(entry.getKey());
			return Component.translatable(flag ? "hunted.overlay.reward.collected" : "hunted.overlay.reward.uncollected").withStyle(Style.EMPTY.withBold(true).withColor(flag ? ChatFormatting.GREEN : ChatFormatting.RED)).append(component);
		}).collect(Collectors.toList());
	}

	public static record Settings(Map<ButtonReward, Style> rewards) {}
}
