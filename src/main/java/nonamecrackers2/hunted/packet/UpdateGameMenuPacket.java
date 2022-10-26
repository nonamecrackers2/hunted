package nonamecrackers2.hunted.packet;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import nonamecrackers2.hunted.client.packet.HuntedClientPacketProcessor;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.util.EventType;
import nonamecrackers2.hunted.util.HuntedClassSelector;

public class UpdateGameMenuPacket extends Packet
{
	private @Nullable EventType processed;
	private Map<UUID, HuntedClassSelector> queued;
	private @Nullable ResourceLocation map;
	private @Nullable UUID vip;
	private boolean gameRunning;
	private boolean gameStarting;
	private boolean buttonHighlighting;
	
	public UpdateGameMenuPacket(@Nullable EventType processed, Map<UUID, HuntedClassSelector> queued, @Nullable HuntedMap selectedMap, @Nullable UUID vip, boolean gameRunning, boolean gameStarting, boolean buttonHighlighting)
	{
		super(true);
		this.processed = processed;
		this.queued = queued;
		if (selectedMap != null)
			this.map = selectedMap.id();
		this.vip = vip;
		this.gameRunning = gameRunning;
		this.gameStarting = gameStarting;
		this.buttonHighlighting = buttonHighlighting;
	}
	
	public UpdateGameMenuPacket()
	{
		super(false);
	}
	
	public @Nullable EventType getProcessed()
	{
		return this.processed;
	}
	
	public Map<UUID, HuntedClassSelector> getQueued()
	{
		return this.queued;
	}
	
	public @Nullable ResourceLocation getMap()
	{
		return this.map;
	}
	
	public UUID getVip()
	{
		return this.vip;
	}
	
	public boolean gameRunning()
	{
		return this.gameRunning;
	}
	
	public boolean gameStarting()
	{
		return this.gameStarting;
	}
	
	public boolean buttonHighlighting()
	{
		return this.buttonHighlighting;
	}
	
	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
		if (buffer.readBoolean())
			this.processed = buffer.readEnum(EventType.class);
		int size = buffer.readVarInt();
		Map<UUID, HuntedClassSelector> queued = Maps.newHashMap();
		for (int i = 0; i < size; i++)
			queued.put(buffer.readUUID(), HuntedClassSelector.fromPacket(buffer));
		this.queued = queued;
		if (buffer.readBoolean())
			this.map = buffer.readResourceLocation();
		if (buffer.readBoolean())
			this.vip = buffer.readUUID();
		this.gameRunning = buffer.readBoolean();
		this.gameStarting = buffer.readBoolean();
		this.buttonHighlighting = buffer.readBoolean();
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
		buffer.writeBoolean(this.processed != null);
		if (this.processed != null)
			buffer.writeEnum(this.processed);
		buffer.writeVarInt(this.queued.size());
		this.queued.forEach((player, selector) -> 
		{
			buffer.writeUUID(player);
			selector.toPacket(buffer);
		});
		buffer.writeBoolean(this.map != null);
		if (this.map != null)
			buffer.writeResourceLocation(this.map);
		buffer.writeBoolean(this.vip != null);
		if (this.vip != null)
			buffer.writeUUID(this.vip);
		buffer.writeBoolean(this.gameRunning);
		buffer.writeBoolean(this.gameStarting);
		buffer.writeBoolean(this.buttonHighlighting);
	}
	
	@Override
	public Runnable getProcessor(NetworkEvent.Context context)
	{
		return () -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HuntedClientPacketProcessor.processUpdateGameMenuPacket(this));
	}
	
	@Override
	public String toString()
	{
		return "UpdateGameMenuPacket[processed=" + this.processed + ", "
				+ "queued=" + this.queued + ", "
				+ "map=" + this.map + ", "
				+ "vip=" + this.vip + ", "
				+ "gameRunning=" + this.gameRunning + ", "
				+ "gameStarting=" + this.gameStarting + ", "
				+ "buttonHighlighting=" + this.buttonHighlighting + "]";
	}
}
