package nonamecrackers2.hunted.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.hunted.client.packet.HuntedClientPacketProcessor;

public class DoJumpscarePacket extends Packet
{
	private SoundEvent event;
	private int time;
	
	public DoJumpscarePacket(SoundEvent event, int time)
	{
		super(true);
		this.event = event;
		this.time = time;
	}
	
	public DoJumpscarePacket()
	{
		super(false);
	}
	
	public SoundEvent getEvent()
	{
		return this.event;
	}

	public int getTime()
	{
		return this.time;
	}

	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
		this.event = buffer.readRegistryId();
		this.time = buffer.readVarInt();
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
		buffer.writeRegistryId(ForgeRegistries.SOUND_EVENTS, this.event);
		buffer.writeVarInt(this.time);
	}
	
	@Override
	public Runnable getProcessor(Context context)
	{
		return () -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HuntedClientPacketProcessor.processDoJumpscarePacket(this));
	}
	
	@Override
	public String toString()
	{
		return "DoJumpscarePacket[" 
				+ "sound: " + this.event + ", "
				+ "time: " + this.time + "]";
	}
}
