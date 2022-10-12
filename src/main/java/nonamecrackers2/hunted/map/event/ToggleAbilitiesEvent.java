package nonamecrackers2.hunted.map.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.ability.type.ToggleAbilities;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

public class ToggleAbilitiesEvent extends MapEvent<ToggleAbilities.Settings>
{
	public ToggleAbilitiesEvent()
	{
		super(ToggleAbilities.CODEC);
	}

	@Override
	public void activate(ToggleAbilities.Settings settings, TriggerContext context, CompoundTag data)
	{
		HuntedClass huntedClass = context.getHuntedClass();
		for (ResourceLocation id : settings.ids())
		{
			for (Ability ability : huntedClass.getAbilities())
			{
				if (ability.id().equals(id))
				{
					ability.setDisabled(!ability.isDisabled());
					break;
				}
			}
		}
	}

	@Override
	public Trigger.Criteria triggerCriteria(ToggleAbilities.Settings settings)
	{
		return Trigger.criteria().player();
	}
}
