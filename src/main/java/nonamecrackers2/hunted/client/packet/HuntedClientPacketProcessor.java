package nonamecrackers2.hunted.client.packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import nonamecrackers2.hunted.client.capability.HuntedClientClassManager;
import nonamecrackers2.hunted.client.init.HuntedClientCapabilities;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.HuntedClassDataManager;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.map.HuntedMapDataManager;
import nonamecrackers2.hunted.packet.DoJumpscarePacket;
import nonamecrackers2.hunted.packet.UpdateGameInfoPacket;
import nonamecrackers2.hunted.packet.UpdatePlayerClassManagerPacket;

public class HuntedClientPacketProcessor
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static void processUpdatePlayerClassManagerPacket(UpdatePlayerClassManagerPacket packet)
	{
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		Entity entity = level.getEntity(packet.getId());
		if (entity instanceof AbstractClientPlayer player)
		{
			//LOGGER.debug("Received update info for player: " + player);
			player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
			{
				if (manager instanceof HuntedClientClassManager clientManager)
				{
					clientManager.setGameRunning(packet.gameIsRunning());
					clientManager.setHasEscaped(packet.hasEscaped());
					ResourceLocation id = packet.getClassId();
					if (id != null)
					{
						HuntedClass huntedClass = HuntedClassDataManager.INSTANCE.syncedValues().get(id);
						if (huntedClass != null)
							clientManager.setClass(huntedClass);
						else
							LOGGER.warn("Could not find received HuntedClass");
					}
					else
					{
						clientManager.setClass(null);
					}
					clientManager.setMask(packet.getMask());
					//LOGGER.debug("Player's class: " + clientManager.getCurrentClass());
				}
			});
		}
	}
	
	public static void processUpdateGameInfoPacket(UpdateGameInfoPacket packet)
	{
		Minecraft mc = Minecraft.getInstance();
		mc.level.getCapability(HuntedClientCapabilities.GAME_INFO).ifPresent(manager -> 
		{
			manager.setGameRunning(packet.gameRunning());
			manager.setOverlay(packet.getText());
			if (packet.getMapId() != null)
			{
				HuntedMap map = HuntedMapDataManager.INSTANCE.getSynced(packet.getMapId());
				if (map == null)
					LOGGER.warn("Could not find received HuntedMap");
				manager.setMap(map);
			}
			else
			{
				manager.setMap(null);
			}
		});
	}
	
	public static void processDoJumpscarePacket(DoJumpscarePacket packet)
	{
		Minecraft mc = Minecraft.getInstance();
		mc.level.getCapability(HuntedClientCapabilities.EFFECTS_MANAGER).ifPresent(manager -> {
			manager.doJumpscare(packet.getEvent(), packet.getTime());
		});
	}
}
