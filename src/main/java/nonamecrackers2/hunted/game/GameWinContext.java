package nonamecrackers2.hunted.game;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.server.level.ServerPlayer;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.huntedclass.type.HunterClassType;

public record GameWinContext(HuntedClassType status, Map<ServerPlayer, HuntedClass> winners, Map<ServerPlayer, HuntedClass> losers)
{
	public static GameWinContext empty()
	{
		return new GameWinContext(null, Maps.newHashMap(), Maps.newHashMap());
	}
	
	public boolean isEmpty()
	{
		return this.status == null;
	}
	
	public boolean isPreyWin()
	{
		if (!this.isEmpty())
			return this.winners().values().stream().filter(huntedClass -> huntedClass.getType() instanceof HunterClassType).toList().size() == 0;
		else
			return false;
	}
	
	public boolean isHunterWin()
	{
		if (!this.isEmpty())
			return !this.isPreyWin();
		else
			return false;
	}
}
