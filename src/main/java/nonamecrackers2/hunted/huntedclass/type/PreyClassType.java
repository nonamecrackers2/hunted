package nonamecrackers2.hunted.huntedclass.type;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;

public class PreyClassType extends HuntedClassType
{
	public PreyClassType(Properties properties) 
	{
		super(properties);
	}

	@Override
	public boolean checkObjective(ServerLevel level, HuntedGame game, LivingEntity player, HuntedClass huntedClass)
	{
		if (game.getActiveBy(PreyClassType.class).size() == 0 && game.getEscaped().size() > 0 || game.getActiveBy(HunterClassType.class).size() == 0)
			return true;
		else
			return false;
	}
	
	@Override
	public boolean checkPartialObjective(ServerLevel level, HuntedGame game, LivingEntity player, HuntedClass huntedClass)
	{
		return game.hasPlayerEscaped(player);
	}
}
