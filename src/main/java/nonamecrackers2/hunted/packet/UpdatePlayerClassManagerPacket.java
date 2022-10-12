package nonamecrackers2.hunted.packet;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import nonamecrackers2.hunted.client.packet.HuntedClientPacketProcessor;
import nonamecrackers2.hunted.huntedclass.HuntedClass;

public class UpdatePlayerClassManagerPacket extends Packet
{
	private int id;
	private boolean gameIsRunning;
	private boolean hasEscaped;
	private @Nullable ResourceLocation classId;
	private @Nullable ResourceLocation mask;
	
	public UpdatePlayerClassManagerPacket(int id, boolean gameIsRunning, boolean hasEscaped, Optional<HuntedClass> huntedClass, @Nullable ResourceLocation mask)
	{
		super(true);
		this.id = id;
		this.gameIsRunning = gameIsRunning;
		this.hasEscaped = hasEscaped;
		huntedClass.ifPresent(value -> this.classId = value.id());
		this.mask = mask;
	}
	
	public UpdatePlayerClassManagerPacket()
	{
		super(false);
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public boolean gameIsRunning()
	{
		return this.gameIsRunning;
	}
	
	public boolean hasEscaped()
	{
		return this.hasEscaped;
	}
	
	public @Nullable ResourceLocation getClassId()
	{
		return this.classId;
	}
	
	public @Nullable ResourceLocation getMask()
	{
		return this.mask;
	}
	
	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
		this.id = buffer.readVarInt();
		this.gameIsRunning = buffer.readBoolean();
		this.hasEscaped = buffer.readBoolean();
		if (buffer.readBoolean())
			this.classId = buffer.readResourceLocation();
		if (buffer.readBoolean())
			this.mask = buffer.readResourceLocation();
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
		buffer.writeVarInt(this.id);
		buffer.writeBoolean(this.gameIsRunning);
		buffer.writeBoolean(this.hasEscaped);
		boolean flag = this.classId != null;
		buffer.writeBoolean(flag);
		if (flag)
			buffer.writeResourceLocation(this.classId);
		buffer.writeBoolean(this.mask != null);
		if (this.mask != null)
			buffer.writeResourceLocation(this.mask);
	}
	
	@Override
	public Runnable getProcessor(NetworkEvent.Context context)
	{
		return () -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HuntedClientPacketProcessor.processUpdatePlayerClassManagerPacket(this));
	}
	
	@Override
	public String toString()
	{
		return "UpdatePlayerClassManagerPacket[" 
				+ "id: " + this.id + ", "
				+ "gameIsRunning: " + this.gameIsRunning + ", "
				+ "hasEscaped: " + this.hasEscaped + ", "
				+ "classId: " + this.classId + ", "
				+ "mask: " + this.mask + "]";
	}
}
