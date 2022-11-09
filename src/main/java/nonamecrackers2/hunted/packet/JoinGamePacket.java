package nonamecrackers2.hunted.packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.huntedclass.HuntedClassDataManager;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.util.EventType;
import nonamecrackers2.hunted.util.HuntedClassSelector;

public class JoinGamePacket extends Packet
{
	private static final Logger LOGGER = LogManager.getLogger();
	private HuntedClassSelector selector;
	
	public JoinGamePacket(HuntedClassSelector selector)
	{
		super(true);
		this.selector = selector;
	}
	
	public JoinGamePacket()
	{
		super(false);
	}
	
	public HuntedClassSelector getSelector()
	{
		return this.selector;
	}

	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
		this.selector = HuntedClassSelector.fromPacket(buffer, HuntedClassDataManager.INSTANCE::get);
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
		this.selector.toPacket(buffer);
		
	}
	
	@Override
	public Runnable getProcessor(NetworkEvent.Context context)
	{
		return () -> 
		{
			ServerPlayer player = context.getSender();
			player.level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
			{
				manager.updateGameMenus(PacketDistributor.PLAYER.with(() -> player), EventType.JOIN);
				try 
				{
					if (!manager.join(player, this.getSelector()))
						LOGGER.warn("Player attempt to rejoin with same classes! ({})", player);
				}
				catch (NullPointerException e)
				{
					LOGGER.warn("Could not join game: received invalid class selector: " + this.selector);
				}
			});
		};
	}
	
	@Override
	public String toString()
	{
		return "JoinGamePacket[" 
				+ "selector: " + this.selector + "]";
	}
}
