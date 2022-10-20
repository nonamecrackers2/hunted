package nonamecrackers2.hunted.packet;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.resources.SimpleDataManager;
import nonamecrackers2.hunted.rewards.ButtonReward;
import nonamecrackers2.hunted.rewards.ButtonRewardsDataManager;

public class ButtonRewardDataManagerPacket extends SimpleDataManagerPacket<ButtonReward>
{
	public ButtonRewardDataManagerPacket(Map<ResourceLocation, ButtonReward> values)
	{
		super(values);
	}
	
	public ButtonRewardDataManagerPacket()
	{
		super();
	}
	
	@Override
	protected BiConsumer<ButtonReward, FriendlyByteBuf> encode()
	{
		return ButtonReward::toPacket;
	}

	@Override
	protected Function<FriendlyByteBuf, ButtonReward> decode()
	{
		return ButtonReward::fromPacket;
	}

	@Override
	protected SimpleDataManager<ButtonReward> manager()
	{
		return ButtonRewardsDataManager.INSTANCE;
	}
}
