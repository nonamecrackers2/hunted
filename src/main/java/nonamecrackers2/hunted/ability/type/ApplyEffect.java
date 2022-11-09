package nonamecrackers2.hunted.ability.type;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.MobEffectHolder;
import nonamecrackers2.hunted.util.TargetSupplier;

public class ApplyEffect extends AbilityType<ApplyEffect.Settings>
{
	public static final Codec<ApplyEffect.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(StringRepresentable.fromEnum(ApplyEffect.ApplyType::values).optionalFieldOf("type").forGetter(s -> Optional.of(s.type)),
				Codec.list(MobEffectHolder.CODEC).fieldOf("effects").forGetter(ApplyEffect.Settings::effects))
				.apply(instance, (type, effects) -> new ApplyEffect.Settings(type.orElse(ApplyEffect.ApplyType.INSTANT), effects));
	});
	public static final String EFFECT_INDEX = "EffectIndex";
	
	public ApplyEffect()
	{
		super(CODEC);
	}

	@Override
	public AbilityType.Result use(ApplyEffect.Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
	{
		return settings.type.use(settings, context, tag, supplier);
	}
	
	@Override
	public void tick(ApplyEffect.Settings settings, ServerLevel level, HuntedGame game, LivingEntity player, HuntedClass huntedClass, CompoundTag tag, TargetSupplier supplier)
	{
		settings.type.tick(settings, level, game, player, huntedClass, tag, supplier);
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(ApplyEffect.Settings settings)
	{
		return Trigger.criteria();
	}
	
	public static record Settings(ApplyEffect.ApplyType type, List<MobEffectHolder> effects) {}
	
	public static enum ApplyType implements StringRepresentable
	{
		INSTANT("instant") 
		{
			@Override
			public AbilityType.Result use(Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
			{
				AbilityType.Result result = AbilityType.Result.FAIL;
				for (LivingEntity player : supplier.getPlayers(context))
				{
					for (MobEffectHolder holder : settings.effects())
					{
						MobEffectInstance effect = holder.createInstance();
						if (player.addEffect(effect))
							result = AbilityType.Result.SUCCESS;
					}
				}
				return result;
			}
		},
		SEQUENTIAL("sequential") 
		{
			@Override
			public AbilityType.Result use(Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
			{
				AbilityType.Result result = AbilityType.Result.FAIL;
				if (tag.getInt(EFFECT_INDEX) < settings.effects().size())
				{
					for (LivingEntity player : supplier.getPlayers(context))
					{
						MobEffectInstance effect = settings.effects().get(tag.getInt(EFFECT_INDEX)).createInstance();
						if (player.addEffect(effect))
							result = AbilityType.Result.SUCCESS;
					}
					HuntedUtil.count(tag, EFFECT_INDEX);
					if (tag.getInt(EFFECT_INDEX) >= settings.effects().size())
						tag.putInt(EFFECT_INDEX, 0);
				}
				return result;
			}
		},
		SEQUENTIAL_IMMEDIATE("sequential_immediate") 
		{
			@Override
			public AbilityType.Result use(Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
			{
				tag.putInt(EFFECT_INDEX, 0);
				AbilityType.Result result = AbilityType.Result.FAIL;
				if (tag.getInt(EFFECT_INDEX) < settings.effects().size())
				{
					for (LivingEntity player : supplier.getPlayers(context))
					{
						MobEffectInstance effect = settings.effects().get(tag.getInt(EFFECT_INDEX)).createInstance();
						if (player.addEffect(effect))
							result = AbilityType.Result.SUCCESS;
					}
					HuntedUtil.count(tag, EFFECT_INDEX);
				}
				return result;
			}
			
			@Override
			public void tick(ApplyEffect.Settings settings, ServerLevel level, HuntedGame game, LivingEntity player, HuntedClass huntedClass, CompoundTag tag, TargetSupplier supplier)
			{
				if (tag.getInt(EFFECT_INDEX) > 0 && tag.getInt(EFFECT_INDEX) < settings.effects().size())
				{
					int prevIndex = tag.getInt(EFFECT_INDEX) - 1;
					if (!player.hasEffect(settings.effects().get(prevIndex).effect()))
					{
						player.addEffect(settings.effects.get(tag.getInt(EFFECT_INDEX)).createInstance());
						HuntedUtil.count(tag, EFFECT_INDEX);
					}
				}
			}
		};
		
		private final String id;
		
		private ApplyType(String id)
		{
			this.id = id;
		}
		
		@Override
		public String getSerializedName()
		{
			return this.id;
		}
		
		public abstract AbilityType.Result use(ApplyEffect.Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier);
		
		public void tick(ApplyEffect.Settings settings, ServerLevel level, HuntedGame game, LivingEntity player, HuntedClass huntedClass, CompoundTag tag, TargetSupplier supplier) {}
	}
}
