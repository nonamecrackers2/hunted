package nonamecrackers2.hunted.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.ability.type.AbilityType;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

/**
 * A helper class for getting a list of players from a {@link TriggerContext}.
 * <br><br>
 * All {@link AbilityType}'s support this by default. TargetSupplier's are configurable
 * and allow for users to specify which players the ability type should effect.
 * <br><br>
 * Example: When a reward is collected, specifying "self" will cause the player receiving
 * the trigger (all the players in the game) to be effected. "target" will only effect
 * the player who collected the reward. "class" and "class_type" will effect all players
 * with that class or class type, specified by the configuration, etc.
 */
public record TargetSupplier(TargetSupplier.Type type, Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> huntedClassType, Optional<TargetSupplier> and)
{
	private static final Codec<TargetSupplier> BASE_CODEC = RecordCodecBuilder.create(instance -> 
	{
		return instance.group(StringRepresentable.fromEnum(TargetSupplier.Type::values).fieldOf("type").forGetter(TargetSupplier::type),
				ResourceLocation.CODEC.optionalFieldOf("class").forGetter(TargetSupplier::huntedClass),
				HuntedRegistries.HUNTED_CLASS_TYPES.get().getCodec().optionalFieldOf("class_type").forGetter(TargetSupplier::huntedClassType))
				.apply(instance, (type, huntedClass, classType) -> type.make(huntedClass, classType, Optional.empty()));
	});
	public static final Codec<TargetSupplier> CODEC = Codec.pair(BASE_CODEC, BASE_CODEC.optionalFieldOf("and").codec()).xmap((pair) -> pair.getFirst().and(pair.getSecond()), supplier -> Pair.of(supplier, supplier.and()));
	public static final TargetSupplier DEFAULT = TargetSupplier.Type.SELF.make(Optional.empty(), Optional.empty(), Optional.empty());
	
	public Trigger.Criteria getTriggerCriteria()
	{
		return this.type.criteria;
	}
	
	public List<LivingEntity> getPlayers(TriggerContext context)
	{
		return this.getPlayers(context, true);
	}
	
	public List<LivingEntity> getPlayers(TriggerContext context, boolean activePlayers)
	{
		var players = this.type.getPlayers(this, context, activePlayers);
		this.and().ifPresent(and -> players.addAll(and.getPlayers(context, activePlayers)));
		return players;
	}
	
	private TargetSupplier and(Optional<TargetSupplier> and)
	{
		return new TargetSupplier(this.type, this.huntedClass, this.huntedClassType, and);
	}
	
	public static enum Type implements StringRepresentable
	{
		SELF("self", Trigger.criteria().player()) 
		{
			@Override
			protected List<LivingEntity> getPlayers(TargetSupplier target, TriggerContext context, boolean activePlayers)
			{
				return Lists.newArrayList(context.player());
			}
		},
		TARGET("target", Trigger.criteria().target()) 
		{
			@Override
			protected List<LivingEntity> getPlayers(TargetSupplier target, TriggerContext context, boolean activePlayers)
			{
				return Lists.newArrayList(context.target());
			}
		},
		CLASS("class", Trigger.criteria())
		{
			@Override
			protected TargetSupplier make(Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> type, Optional<TargetSupplier> and)
			{
				if (huntedClass.isEmpty())
					throw new JsonSyntaxException("Hunted class is missing for type " + this.getSerializedName());
				return new TargetSupplier(this, huntedClass, type, and);
			}
			
			@Override
			protected List<LivingEntity> getPlayers(TargetSupplier target, TriggerContext context, boolean activePlayers)
			{
				List<LivingEntity> players = activePlayers ? context.getGame().getActive() : context.getGame().getPlayers();
				return players.stream().filter(s -> context.getHuntedClass(s).id().equals(target.huntedClass().get())).collect(Collectors.toList());
			}
		},
		CLASS_TYPE("class_type", Trigger.criteria())
		{
			@Override
			protected TargetSupplier make(Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> type, Optional<TargetSupplier> and)
			{
				if (type.isEmpty())
					throw new JsonSyntaxException("Hunted class type is missing for type " + this.getSerializedName());
				return new TargetSupplier(this, huntedClass, type, and);
			}
			
			@Override
			protected List<LivingEntity> getPlayers(TargetSupplier target, TriggerContext context, boolean activePlayers)
			{
				return activePlayers ? context.getGame().getActiveBy(target.huntedClassType().get().getClass()) : context.getGame().getPlayersBy(target.huntedClassType().get().getClass());
			}
		},
		ALL("all", Trigger.criteria())
		{
			@Override
			protected List<LivingEntity> getPlayers(TargetSupplier target, TriggerContext context, boolean activePlayers)
			{
				return Lists.newArrayList(activePlayers ? context.getGame().getActive() : context.getGame().getPlayers());
			}
		},
		RANDOM("random", Trigger.criteria())
		{
			@Override
			protected List<LivingEntity> getPlayers(TargetSupplier target, TriggerContext context, boolean activePlayers)
			{
				List<LivingEntity> players = activePlayers ? context.getGame().getActive() : context.getGame().getPlayers();
				if (target.huntedClass().isPresent())
					players = players.stream().filter(p -> context.getHuntedClass(p).id().equals(target.huntedClass().get())).toList();
				if (target.huntedClassType().isPresent())
					players = players.stream().filter(p -> context.getHuntedClass(p).getType().equals(target.huntedClassType().get())).toList();
				if (context.player() != null)
					players = players.stream().filter(p -> !p.equals(context.player())).toList();
				if (players.size() > 0)
					return Lists.newArrayList(players.get(RandomSource.create().nextInt(players.size())));
				else
					return Lists.newArrayList();
			}
		};

		private final String id;
		public final Trigger.Criteria criteria;
		
		private Type(String id, Trigger.Criteria criteria)
		{
			this.id = id;
			this.criteria = criteria;
		}
		
		protected abstract List<LivingEntity> getPlayers(TargetSupplier target, TriggerContext context, boolean activePlayers);
		
		protected TargetSupplier make(Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> type, Optional<TargetSupplier> and)
		{
			return new TargetSupplier(this, huntedClass, type, and);
		}
		
		@Override
		public String getSerializedName()
		{
			return this.id;
		}
	}
}
