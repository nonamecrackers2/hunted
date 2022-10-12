package nonamecrackers2.hunted.packet;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.map.HuntedMapDataManager;
import nonamecrackers2.hunted.resources.SimpleDataManager;

public class HuntedMapManagerPacket extends SimpleDataManagerPacket<HuntedMap>
{
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
		return HuntedMap::toPacket;
	}
	
	@Override
	protected Function<FriendlyByteBuf, HuntedMap> decode()
	{
		return HuntedMap::fromPacket;
	}
	
	@Override
	protected SimpleDataManager<HuntedMap> manager()
	{
		return HuntedMapDataManager.INSTANCE;
	}
}
