package nonamecrackers2.hunted.map.event;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.ability.type.ModifyCooldown;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.TargetSupplier;

public class ModifyAbilityCooldownsEvent extends MapEvent<ModifyAbilityCooldownsEvent.Settings>
{
	public static final Codec<ModifyAbilityCooldownsEvent.Settings> CODEC = Codec.pair(TargetSupplier.CODEC.fieldOf("target").codec().orElse(TargetSupplier.DEFAULT), ModifyCooldown.CODEC).xmap(pair -> new ModifyAbilityCooldownsEvent.Settings(pair.getFirst(), pair.getSecond()), s -> Pair.of(s.supplier, s.settings));
	
	public ModifyAbilityCooldownsEvent()
	{
		super(CODEC);
	}

	@Override
	public void activate(ModifyAbilityCooldownsEvent.Settings settings, TriggerContext context, CompoundTag data)
	{
		for (ServerPlayer player : settings.supplier().getPlayers(context))
		{
			HuntedClass huntedClass = context.getHuntedClass(player);
			for (Ability ability : huntedClass.getAbilities())
			{
				if (!settings.settings().allAbilities())
				{
					if (settings.settings().ids().contains(ability.id()))
						ModifyCooldown.apply(settings.settings(), player, ability);
				}
				else
				{
					ModifyCooldown.apply(settings.settings(), player, ability);
				}
			}
		}
	}

	@Override
	public Trigger.Criteria triggerCriteria(ModifyAbilityCooldownsEvent.Settings settings)
	{
		return settings.supplier().getTriggerCriteria();
	}
	
	public static record Settings(TargetSupplier supplier, ModifyCooldown.Settings settings) {}
}
