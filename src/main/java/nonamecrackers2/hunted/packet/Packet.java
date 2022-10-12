package nonamecrackers2.hunted.packet;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public abstract class Packet
{
	protected static final Logger LOGGER = LogManager.getLogger();
	
	protected boolean isMessageValid;
	
	protected Packet(boolean valid)
	{
		this.isMessageValid = valid;
	}
	
	public boolean isMessageValid()
	{
		return this.isMessageValid;
	}
	
	public static <T extends Packet> T decode(Supplier<T> blank, FriendlyByteBuf buffer)
	{
		T message = blank.get();
		try
		{
			message.decode(buffer);
		}
		catch (IllegalArgumentException | IndexOutOfBoundsException e)
		{
			LOGGER.warn("Exception while reading " + message.toString() + "; " + e);
			return message;
		}
		message.isMessageValid = true;
		return message;
	}
	
	public void encode(FriendlyByteBuf buffer)
	{
		if (!this.isMessageValid) return;
	}
	
	public abstract void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException;
	
	public abstract Runnable getProcessor(NetworkEvent.Context context);
}
