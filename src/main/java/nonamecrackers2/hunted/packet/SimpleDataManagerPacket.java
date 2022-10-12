package nonamecrackers2.hunted.packet;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
import com.ibm.icu.impl.locale.XCldrStub.ImmutableMap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;
import nonamecrackers2.hunted.resources.SimpleDataManager;

public abstract class SimpleDataManagerPacket<S> extends Packet
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	protected Map<ResourceLocation, S> values;
	
	public SimpleDataManagerPacket(Map<ResourceLocation, S> values)
	{
		super(true);
		this.values = values;
	}
	
	public SimpleDataManagerPacket()
	{
		super(false);
	}
	
	protected abstract BiConsumer<S, FriendlyByteBuf> encode();
	
	protected abstract Function<FriendlyByteBuf, S> decode();
	
	protected abstract SimpleDataManager<S> manager();
	
	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
		Map<ResourceLocation, S> values = Maps.newHashMap();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
		{
			ResourceLocation id = buffer.readResourceLocation();
			S s = this.decode().apply(buffer);
			values.put(id, s);
		}
		this.values = values;
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
		buffer.writeVarInt(this.values.size());
		this.values.forEach((id, s) -> 
		{
			buffer.writeResourceLocation(id);
			this.encode().accept(s, buffer);
		});
	}
	
	@Override
	public Runnable getProcessor(Context context)
	{
		return () -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
		{
			LOGGER.debug("Received {} {}(s)", this.values.size(), this.manager().getDirectory());
			this.manager().setSynced(ImmutableMap.copyOf(this.values));
		});
	}
	
	@Override
	public String toString()
	{
		return "SimpleDataManagerPacket["
				+ "manager: " + this.manager() + ", "
				+ "values: " + this.values + "]";
	}
}
