package nonamecrackers2.hunted.packet;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ibm.icu.impl.locale.XCldrStub.ImmutableMap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.map.HuntedMapDataManager;
import nonamecrackers2.hunted.resources.SimpleDataManager;
import nonamecrackers2.hunted.rewards.ButtonReward;
import nonamecrackers2.hunted.rewards.ButtonRewardsDataManager;

public class HuntedMapManagerPacket extends SimpleDataManagerPacket<HuntedMap>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Map<ResourceLocation, List<ResourceLocation>> rewards = Maps.newHashMap();
	
	public HuntedMapManagerPacket(Map<ResourceLocation, HuntedMap> values)
	{
		super(values);
	}
	
	public HuntedMapManagerPacket()
	{
		super();
	}
	
	@Override
	protected BiConsumer<HuntedMap, FriendlyByteBuf> encode()
	{
		return (map, buffer) -> 
		{
			map.toPacket(buffer);
			buffer.writeCollection(map.rewards(), (buf, reward) -> buf.writeResourceLocation(reward.getId()));
		};
	}
	
	@Override
	protected Function<FriendlyByteBuf, HuntedMap> decode()
	{
		return buffer -> 
		{
			HuntedMap map = HuntedMap.fromPacket(buffer);
			List<ResourceLocation> rewards = buffer.readList(FriendlyByteBuf::readResourceLocation);
			this.rewards.computeIfAbsent(map.id(), (id) -> rewards);
			return map;
		};
	}
	
	@Override
	protected SimpleDataManager<HuntedMap> manager()
	{
		return HuntedMapDataManager.INSTANCE;
	}
	
	@Override
	public Runnable getProcessor(NetworkEvent.Context context)
	{
		return () -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
		{
			LOGGER.debug("Received {} {}(s)", this.values.size(), this.manager().getDirectory());
			for (var entry : this.rewards.entrySet())
			{
				List<ButtonReward> rewards = Lists.newArrayList();
				for (ResourceLocation id : entry.getValue())
				{
					ButtonReward reward = ButtonRewardsDataManager.INSTANCE.getSynced(id);
					if (reward != null)
						rewards.add(reward);
					else
						LOGGER.error("Received unknown reward '{}'!", id);
				}
				this.values.compute(entry.getKey(), (id, map) -> map.copyWithRewards(rewards));
			}
			this.manager().setSynced(ImmutableMap.copyOf(this.values));
		});
	}
}
