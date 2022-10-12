package nonamecrackers2.hunted.ability.type;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.TargetSupplier;

public class Eliminate extends NoSettingsAbilityType
{
	public Eliminate()
	{
		super(true);
	}

	@Override
	public AbilityType.Result use(NoSettingsAbilityType.EmptySettings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
	{
		AbilityType.Result result = AbilityType.Result.FAIL;
		for (ServerPlayer player : supplier.getPlayers(context))
		{
			if (!context.getGame().isPlayerEliminated(player))
			{
				context.getGame().eliminate(player);
				result = AbilityType.Result.SUCCESS;
			}
		}
		return result;
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(NoSettingsAbilityType.EmptySettings settings)
	{
		return Trigger.criteria();
	}
}
