package nonamecrackers2.hunted.packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.util.EventType;

public class PickGameModePacket extends Packet
{
	private static final Logger LOGGER = LogManager.getLogger();
	private HuntedGame.GameMode gameMode;
	
	public PickGameModePacket(HuntedGame.GameMode gameMode)
	{
		super(true);
		this.gameMode = gameMode;
	}
	
	public PickGameModePacket()
	{
		super(false);
	}

	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
		this.gameMode = buffer.readEnum(HuntedGame.GameMode.class);
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
		buffer.writeEnum(this.gameMode);
	}
	
	@Override
	public Runnable getProcessor(NetworkEvent.Context context)
	{
		return () -> 
		{
			ServerPlayer player = context.getSender();
			player.level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
			{
				if (player.equals(manager.getVip()))
					manager.setMode(this.gameMode);
				else
					LOGGER.warn("Non-VIP player tried to pick game mode. Player: {}", player);
				manager.updateGameMenus(PacketDistributor.PLAYER.with(() -> player), EventType.PICK_GAME_MODE);
				manager.updateGameMenus(PacketDistributor.DIMENSION.with(() -> player.level.dimension()));
			});
		};
	}
	
	@Override
	public String toString()
	{
		return "PickGameModePacket[" 
				+ "gameMode: " + this.gameMode + "]";
	}
}
