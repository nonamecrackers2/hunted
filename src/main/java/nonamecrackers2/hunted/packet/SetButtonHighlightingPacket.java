package nonamecrackers2.hunted.packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.config.HuntedConfig;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.util.EventType;

public class SetButtonHighlightingPacket extends Packet
{
	private static final Logger LOGGER = LogManager.getLogger();
	private boolean value;
	
	public SetButtonHighlightingPacket(boolean value)
	{
		super(true);
		this.value = value;
	}
	
	public SetButtonHighlightingPacket()
	{
		super(false);
	}
	
	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
		this.value = buffer.readBoolean();
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
		buffer.writeBoolean(this.value);
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
					HuntedConfig.SERVER.buttonHighlighting.set(this.value);
				else
					LOGGER.warn("Non-VIP player tried setting button highlighting. Player: {}", player);
				manager.updateGameMenus(PacketDistributor.PLAYER.with(() -> player), EventType.SET_BUTTON_HIGHLIGHTING);
			});
		};
	}
}
