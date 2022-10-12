package nonamecrackers2.hunted.ability.type;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.TargetSupplier;

public class ToggleAbilities extends AbilityType<ToggleAbilities.Settings>
{
	public static final Codec<ToggleAbilities.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(Codec.list(ResourceLocation.CODEC).fieldOf("abilities").forGetter(ToggleAbilities.Settings::ids)).apply(instance, ToggleAbilities.Settings::new);
	});
	
	public ToggleAbilities()
	{
		super(CODEC, false);
	}

	@Override
	public AbilityType.Result use(ToggleAbilities.Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
	{
		HuntedClass huntedClass = context.getHuntedClass();
		var result = AbilityType.Result.FAIL;
		for (ResourceLocation id : settings.ids())
		{
			for (Ability ability : huntedClass.getAbilities())
			{
				if (ability.id().equals(id))
				{
					ability.setDisabled(!ability.isDisabled());
					result = AbilityType.Result.PASS;
					break;
				}
			}
		}
		return result;
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(ToggleAbilities.Settings settings)
	{
		return Trigger.criteria().player();
	}
	
	public static record Settings(List<ResourceLocation> ids) {}
}
