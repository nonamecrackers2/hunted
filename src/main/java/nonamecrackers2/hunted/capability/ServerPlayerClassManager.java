package nonamecrackers2.hunted.capability;

import net.minecraft.server.level.ServerLevel;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.trigger.Triggerable;
import nonamecrackers2.hunted.util.DataHolder;

public interface ServerPlayerClassManager extends PlayerClassManager, Triggerable, DataHolder
{
	void tick(ServerLevel level, HuntedGame game);
	
	void begin(HuntedMap map);
	
	void finish();
	
	void clear();
	
	boolean requestsUpdate();
	
	void setUpdateRequest(boolean flag);
	
	void update();
	
	void use(TriggerContext context);
}
