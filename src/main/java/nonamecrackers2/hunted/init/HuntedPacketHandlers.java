package nonamecrackers2.hunted.init;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.packet.AbilityDataManagerPacket;
import nonamecrackers2.hunted.packet.ActivateTriggerPacket;
import nonamecrackers2.hunted.packet.DoJumpscarePacket;
import nonamecrackers2.hunted.packet.HuntedClassManagerPacket;
import nonamecrackers2.hunted.packet.HuntedMapManagerPacket;
import nonamecrackers2.hunted.packet.Packet;
import nonamecrackers2.hunted.packet.UpdateGameInfoPacket;
import nonamecrackers2.hunted.packet.UpdatePlayerClassManagerPacket;

public class HuntedPacketHandlers
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String VERSION = "1.1";
	public static final SimpleChannel MAIN = NetworkRegistry.newSimpleChannel(
			HuntedMod.resource("main"), 
			() -> VERSION, 
			VERSION::equals, 
			VERSION::equals
	);
	public static final int ABILITY_PACKET_ID = 1;
	public static final int UPDATE_GAME_STATUS = 3;
	public static final int HUNTED_CLASS_MANAGER = 4;
	public static final int HUNTED_MAP_MANAGER = 5;
	public static final int HUNTED_GAME_INFO = 6;
	public static final int ABILITY_DATA_MANAGER = 7;
	public static final int DO_JUMPSCARE = 8;
	
	public static void registerPackets()
	{
		MAIN.registerMessage(
				ABILITY_PACKET_ID, 
				ActivateTriggerPacket.class,
				ActivateTriggerPacket::encode,
				(buffer) -> Packet.decode(ActivateTriggerPacket::new, buffer),
				HuntedPacketHandlers::receiveServerMessage,
				Optional.of(NetworkDirection.PLAY_TO_SERVER)
		);
		MAIN.registerMessage(
				UPDATE_GAME_STATUS, 
				UpdatePlayerClassManagerPacket.class,
				UpdatePlayerClassManagerPacket::encode,
				(buffer) -> Packet.decode(UpdatePlayerClassManagerPacket::new, buffer),
				HuntedPacketHandlers::receiveClientMessage,
				Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
		MAIN.registerMessage(
				HUNTED_CLASS_MANAGER, 
				HuntedClassManagerPacket.class,
				HuntedClassManagerPacket::encode,
				(buffer) -> Packet.decode(HuntedClassManagerPacket::new, buffer),
				HuntedPacketHandlers::receiveClientMessage,
				Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
		MAIN.registerMessage(
				HUNTED_MAP_MANAGER, 
				HuntedMapManagerPacket.class,
				HuntedMapManagerPacket::encode,
				(buffer) -> Packet.decode(HuntedMapManagerPacket::new, buffer),
				HuntedPacketHandlers::receiveClientMessage,
				Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
		MAIN.registerMessage(
				HUNTED_GAME_INFO, 
				UpdateGameInfoPacket.class,
				UpdateGameInfoPacket::encode,
				(buffer) -> Packet.decode(UpdateGameInfoPacket::new, buffer),
				HuntedPacketHandlers::receiveClientMessage,
				Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
		MAIN.registerMessage(
				ABILITY_DATA_MANAGER, 
				AbilityDataManagerPacket.class,
				AbilityDataManagerPacket::encode,
				(buffer) -> Packet.decode(AbilityDataManagerPacket::new, buffer),
				HuntedPacketHandlers::receiveClientMessage,
				Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
		MAIN.registerMessage(
				DO_JUMPSCARE, 
				DoJumpscarePacket.class,
				DoJumpscarePacket::encode,
				(buffer) -> Packet.decode(DoJumpscarePacket::new, buffer),
				HuntedPacketHandlers::receiveClientMessage,
				Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
	}
	
	public static <T extends Packet> void receiveClientMessage(final T message, Supplier<NetworkEvent.Context> supplier)
	{
		NetworkEvent.Context context = supplier.get();
		LogicalSide sideReceived = context.getDirection().getReceptionSide();
		context.setPacketHandled(true);
		
		if (sideReceived != LogicalSide.CLIENT)
		{
			LOGGER.warn(message.toString() + " was received on the wrong side: " + sideReceived);
			return;
		}
		
		if (!message.isMessageValid())
		{
			LOGGER.warn(message.toString() + " was invalid");
			return;
		}
		
		context.enqueueWork(message.getProcessor(context));
	}
	
	public static <T extends Packet> void receiveServerMessage(final T message, Supplier<NetworkEvent.Context> supplier)
	{
		NetworkEvent.Context context = supplier.get();
		LogicalSide sideReceived = context.getDirection().getReceptionSide();
		context.setPacketHandled(true);
		
		if (sideReceived != LogicalSide.SERVER)
		{
			LOGGER.warn(message.toString() + " was received on the wrong side: " + sideReceived);
			return;
		}
		
		if (!message.isMessageValid())
		{
			LOGGER.warn(message.toString() + " was invalid");
			return;
		}
		
		final ServerPlayer player = context.getSender();
		if (player == null)
		{
			LOGGER.warn("The sending player is not present when " + message.toString() + " was received");
			return;
		}
		
		context.enqueueWork(message.getProcessor(context));
	}
}
