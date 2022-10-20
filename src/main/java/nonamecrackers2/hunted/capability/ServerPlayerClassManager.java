package nonamecrackers2.hunted.capability;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.init.HuntedPacketHandlers;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.packet.UpdatePlayerClassManagerPacket;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.trigger.Triggerable;
import nonamecrackers2.hunted.util.DataHolder;

public interface ServerPlayerClassManager extends PlayerClassManager, Triggerable, DataHolder
{
	default void tick(ServerLevel level, HuntedGame game)
	{
		this.getCurrentClass().ifPresent(huntedClass -> huntedClass.tick(level, game, this.getPlayer(), this.getOrCreateTag()));
	}
	
	ServerPlayer getPlayer();
	
	void begin(HuntedMap map);
	
	void finish();
	
	void clear();
	
	boolean requestsUpdate();
	
	void setUpdateRequest(boolean flag);
	
	default void update()
	{
		this.setUpdateRequest(false);
		HuntedPacketHandlers.MAIN.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getPlayer), new UpdatePlayerClassManagerPacket(this.getPlayer().getId(), this.isInGame(), this.hasEscaped(), this.getCurrentClass(), this.getMask()));
		this.getPlayer().refreshDisplayName();
		this.getPlayer().refreshTabListName();
	}
	
	void use(TriggerContext context);
}
