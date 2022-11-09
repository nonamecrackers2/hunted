package nonamecrackers2.hunted.huntedclass.type;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;

public class HunterClassType extends HuntedClassType
{
	public HunterClassType(Properties properties) 
	{
		super(properties);
	}
	
	@Override
	public boolean checkObjective(ServerLevel level, HuntedGame game, LivingEntity player, HuntedClass huntedClass)
	{
		List<LivingEntity> players = game.getPlayers().stream().filter(p -> 
		{
			HuntedClass pHuntedClass = PlayerClassManager.getClassFor(p);
			return pHuntedClass != null && !(pHuntedClass.getType() instanceof HunterClassType) && !game.isPlayerEliminated(p);
		}
		).collect(Collectors.toList());
		return players.size() <= 0;
	}
}
