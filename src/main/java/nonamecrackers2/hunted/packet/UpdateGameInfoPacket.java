package nonamecrackers2.hunted.packet;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;
import nonamecrackers2.hunted.client.packet.HuntedClientPacketProcessor;
import nonamecrackers2.hunted.map.HuntedMap;

public class UpdateGameInfoPacket extends Packet
{
	private boolean gameRunning;
	private List<Component> text;
	private @Nullable ResourceLocation mapId;
	
	public UpdateGameInfoPacket(boolean gameRunning, List<Component> text, @Nullable HuntedMap map)
	{
		super(true);
		this.gameRunning = gameRunning;
		this.text = text;
		this.mapId = map != null ? map.id() : null;
	}
	
	public UpdateGameInfoPacket()
	{
		super(false);
	}
	
	public boolean gameRunning()
	{
		return this.gameRunning;
	}
	
	public List<Component> getText()
	{
		return this.text;
	}
	
	public @Nullable ResourceLocation getMapId()
	{
		return this.mapId;
	}
	
	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
		this.gameRunning = buffer.readBoolean();
		List<Component> text = Lists.newArrayList();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			text.add(buffer.readComponent());
		this.text = text;
		if (buffer.readBoolean())
			this.mapId = buffer.readResourceLocation();
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
		buffer.writeBoolean(this.gameRunning);
		buffer.writeVarInt(this.text.size());
		for (Component component : this.text)
			buffer.writeComponent(component);
		buffer.writeBoolean(this.mapId != null);
		if (this.mapId != null)
			buffer.writeResourceLocation(this.mapId);
	}
	
	@Override
	public Runnable getProcessor(Context context)
	{
		return () -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HuntedClientPacketProcessor.processUpdateGameInfoPacket(this));
	}
	
	@Override
	public String toString()
	{
		return "UpdateGameInfoPacket[" 
				+ "gameIsRunning: " + this.gameRunning + ", "
				+ "text: " + this.text + ", "
				+ "mapId: " + this.mapId + "]";
	}
}
