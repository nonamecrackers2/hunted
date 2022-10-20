package nonamecrackers2.hunted.packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.game.HuntedGameManager;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.util.EventType;

public class BeginGamePacket extends Packet
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	public BeginGamePacket()
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
				manager.updateGameMenus(PacketDistributor.PLAYER.with(() -> player), EventType.BEGIN);
				if (player.equals(manager.getVip()))
				{
					HuntedGameManager.GameStartStatus status = manager.startGame(200);
					if (status != HuntedGameManager.GameStartStatus.SUCCESS)
						LOGGER.warn("Could not start game. Reason: {}, Player: {}", status, player);
				}
				else
				{
					LOGGER.warn("Non-VIP player tried to begin game. Player: {}", player);
				}
			});
		};
	}
}
