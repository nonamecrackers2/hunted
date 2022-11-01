package nonamecrackers2.hunted.ability.type;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.TargetSupplier;

public class ModifyCooldown extends AbilityType<ModifyCooldown.Settings>
{
	public static final Codec<ModifyCooldown.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(Codec.list(ResourceLocation.CODEC).fieldOf("abilities").forGetter(ModifyCooldown.Settings::ids),
				Codec.BOOL.optionalFieldOf("all_abilities").forGetter(s -> Optional.of(s.allAbilities)),
				Codec.BOOL.optionalFieldOf("modify_time").forGetter(s -> Optional.of(s.modifyTime)),
				Codec.INT.fieldOf("cooldown").forGetter(ModifyCooldown.Settings::cooldown),
				StringRepresentable.fromEnum(ModifyCooldown.Operation::values).optionalFieldOf("operation").forGetter(s -> Optional.of(s.operation)))
				.apply(instance, (abilities, allAbilities, modifyTime, cooldown, operation) -> new ModifyCooldown.Settings(abilities, allAbilities.orElse(false), modifyTime.orElse(false), cooldown, operation.orElse(ModifyCooldown.Operation.SET)));
	});
	
	public ModifyCooldown()
	{
		super(CODEC, false);
	}

	@Override
	public AbilityType.Result use(ModifyCooldown.Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
	{
		var result = AbilityType.Result.FAIL;
		for (LivingEntity player : supplier.getPlayers(context))
		{
			HuntedClass huntedClass = context.getHuntedClass(player);
			for (Ability ability : huntedClass.getAllAbilities())
			{
				if (!settings.allAbilities())
				{
					if (settings.ids().contains(ability.id()))
						result = apply(settings, player, ability);
				}
				else
				{
					result = apply(settings, player, ability);
				}
			}
		}
		return result;
	}
	
	public static AbilityType.Result apply(ModifyCooldown.Settings settings, LivingEntity player, Ability ability)
	{
		if (!settings.modifyTime())
		{
			ability.setCooldown(settings.operation().operate(ability.getCooldown(), settings.cooldown(), Integer.MAX_VALUE));
		}
		else
		{
			int time = settings.operation().operate(ability.getTimer(), settings.cooldown(), ability.getCooldown());
			ability.setTime(time);
			if (player instanceof ServerPlayer serverPlayer)
				serverPlayer.getCooldowns().addCooldown(ability.getItem(), time);
		}
		return AbilityType.Result.PASS;
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(ModifyCooldown.Settings settings)
	{
		return Trigger.criteria();
	}
	
	public static record Settings(List<ResourceLocation> ids, boolean allAbilities, boolean modifyTime, int cooldown, ModifyCooldown.Operation operation) {}
	
	public static enum Operation implements StringRepresentable
	{
		ADD("add") 
		{
			@Override
			public int operate(int cooldown, int modification, int max)
			{
				return Math.min(cooldown + modification, max);
			}
		},
		SUBTRACT("subtract") 
		{
			@Override
			public int operate(int cooldown, int modification, int max)
			{
				return Math.max(cooldown - modification, 0);
			}
		},
		SET("set") 
		{
			@Override
			public int operate(int cooldown, int modification, int max)
			{
				return Mth.clamp(modification, 0, max);
			}
		};

		private final String id;
		
		private Operation(String id)
		{
			this.id = id;
		}
		
		@Override
		public String getSerializedName()
		{
			return this.id;
		}
		
		public abstract int operate(int cooldown, int modification, int max);
	}
}
