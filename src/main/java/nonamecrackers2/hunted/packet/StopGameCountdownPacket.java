package nonamecrackers2.hunted.packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.util.EventType;

public class StopGameCountdownPacket extends Packet
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	public StopGameCountdownPacket()
	{
		super(true);
	}
	
	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
	}
	
	@Override
	public Runnable getProcessor(NetworkEvent.Context context)
	{
		return () -> 
		{
			ServerPlayer player = context.getSender();
			player.level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
			{
				manager.updateGameMenus(PacketDistributor.PLAYER.with(() -> player), EventType.STOP_COUNTDOWN);
				if (player.equals(manager.getVip()))
					manager.stopCountdown();
				else
					LOGGER.warn("Non-VIP player tried to stop game. Player: {}", player);
			});
		};
	}
}
