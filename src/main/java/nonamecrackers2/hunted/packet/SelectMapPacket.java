package nonamecrackers2.hunted.packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.map.HuntedMapDataManager;
import nonamecrackers2.hunted.util.EventType;

public class SelectMapPacket extends Packet
{
	private static final Logger LOGGER = LogManager.getLogger();
	private ResourceLocation map;
	
	public SelectMapPacket(HuntedMap map)
	{
		super(true);
		this.map = map.id();
	}
	
	public SelectMapPacket()
	{
		super(false);
	}
	
	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
		this.map = buffer.readResourceLocation();
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
		buffer.writeResourceLocation(this.map);
		
	}
	
	@Override
	public Runnable getProcessor(NetworkEvent.Context context)
	{
		return () -> 
		{
			ServerPlayer player = context.getSender();
			player.level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
			{
				manager.updateGameMenus(PacketDistributor.PLAYER.with(() -> player), EventType.SELECT_MAP);
				HuntedMap map = HuntedMapDataManager.INSTANCE.get(this.map);
				if (map != null)
				{
					if (player.equals(manager.getVip()))
						manager.setMap(map);
					else
						LOGGER.warn("Non-VIP player tried to select a map! Player: {}", player);
				}
				else
				{
					LOGGER.warn("Received unknown map '{}'", this.map);
				}
			});
		};
	}
	
	@Override
	public String toString()
	{
		return "SelectMapPacket[" 
				+ "map: " + this.map + "]";
	}
}
