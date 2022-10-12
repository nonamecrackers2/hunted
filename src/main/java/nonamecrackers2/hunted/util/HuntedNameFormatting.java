package nonamecrackers2.hunted.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.game.HuntedGameManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;

public class HuntedNameFormatting
{
	public static Component addStyle(Entity entity, MutableComponent component)
	{
		HuntedGameManager gameManager = entity.level.getCapability(HuntedCapabilities.GAME_MANAGER).orElse(null);
		if (gameManager != null && gameManager.isGameRunning())
		{
			PlayerClassManager manager = entity.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
			if (manager != null)
			{
				HuntedClass huntedClass = manager.getCurrentClass().orElse(null);
				if (huntedClass != null && manager.isInGame())
					return component.withStyle(Style.EMPTY.withColor(huntedClass.getType().getColor()));
				else
					return component.withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));
			}
			else
			{
				return component;
			}
		}
		else
		{
			return component;
		}
	}
}
