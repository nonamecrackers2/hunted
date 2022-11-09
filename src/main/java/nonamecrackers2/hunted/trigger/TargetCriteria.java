package nonamecrackers2.hunted.trigger;

import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.ability.type.Bind;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.capability.ServerPlayerClassManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.registry.HuntedRegistries;

public record TargetCriteria(TargetCriteria.Type type, Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> huntedClassType)
{
	public static final Codec<TargetCriteria> CODEC = RecordCodecBuilder.create(instance -> 
	{
		return instance.group(
				StringRepresentable.fromEnum(TargetCriteria.Type::values).fieldOf("type").forGetter(TargetCriteria::type),
				ResourceLocation.CODEC.optionalFieldOf("class").forGetter(TargetCriteria::huntedClass),
				HuntedRegistries.HUNTED_CLASS_TYPES.get().getCodec().optionalFieldOf("class_type").forGetter(TargetCriteria::huntedClassType))
				.apply(instance, TargetCriteria.Type::make);
	});
	public static final TargetCriteria DEFAULT = TargetCriteria.Type.ANY.make(Optional.empty(), Optional.empty());
	
//	public static TriggerPlayerTarget read(JsonElement element)
//	{
//		JsonObject object = GsonHelper.convertToJsonObject(element, "target");
//		String typeId = GsonHelper.getAsString(object, "type");
//		TriggerPlayerTarget.Type finalType = null;
//		for (TriggerPlayerTarget.Type type : TriggerPlayerTarget.Type.values())
//		{
//			if (type.getSerializedName().equals(typeId))
//			{
//				finalType = type;
//				break;
//			}
//		}
//		if (finalType == null)
//			throw new JsonSyntaxException("Unknown or unsupported trigger player target type '" + typeId + "'");
//		return finalType.read(object);
//	}
	
	public Trigger.Criteria getTriggerCriteria()
	{
		return this.type.getCriteria();
	}
	
	public boolean matches(TriggerContext context)
	{
		return this.type.matches(this, context);
	}
	
	public static enum Type implements StringRepresentable
	{
		SELF("self", Trigger.criteria().player().target())
		{
			@Override
			protected TargetCriteria make(Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> type)
			{
				return new TargetCriteria(this, huntedClass, type);
			}
			
			@Override
			protected boolean matches(TargetCriteria target, TriggerContext context)
			{
				return context.player().equals(context.target());
			}
		},
		ANY("any", Trigger.criteria())
		{
			@Override
			protected TargetCriteria make(Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> type)
			{
				return new TargetCriteria(this, huntedClass, type);
			}
			
			@Override
			protected boolean matches(TargetCriteria target, TriggerContext context)
			{
				return true;
			}
		},
		CLASS("class", Trigger.criteria().player()) 
		{
			@Override
			protected TargetCriteria make(Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> type)
			{
				if (huntedClass.isEmpty())
					throw new JsonSyntaxException("Hunted class is missing for type " + this.getSerializedName());
				return new TargetCriteria(this, huntedClass, type);
			}
			
			@Override
			protected boolean matches(TargetCriteria target, TriggerContext context)
			{
				return context.getHuntedClass().id().equals(target.huntedClass().get());
			}
		},
		CLASS_TYPE("class_type", Trigger.criteria().player())
		{
			@Override
			protected TargetCriteria make(Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> type)
			{
				if (type.isEmpty())
					throw new JsonSyntaxException("Hunted class type is missing for type " + this.getSerializedName());
				return new TargetCriteria(this, huntedClass, type);
			}
			
			@Override
			protected boolean matches(TargetCriteria target, TriggerContext context)
			{
				return context.getHuntedClass().getType().equals(target.huntedClassType.get());
			}
		},
		BINDED("binded", Trigger.criteria().player().target())
		{
			@Override
			protected TargetCriteria make(Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> type)
			{
				return new TargetCriteria(this, huntedClass, type);
			}
			
			@Override
			protected boolean matches(TargetCriteria target, TriggerContext context)
			{
				PlayerClassManager manager = context.player().getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
				if (manager instanceof ServerPlayerClassManager serverManager)
				{
					HuntedClass huntedClass = serverManager.getCurrentClass().orElse(null);
					if (huntedClass != null)
					{
						for (Ability ability : huntedClass.getAllAbilities())
						{
							CompoundTag tag = serverManager.getOrCreateTagElement(ability.id().toString());
							if (tag.contains(Bind.BINDED))
							{
								UUID uuid = tag.getUUID(Bind.BINDED);
								if (context.target().getUUID().equals(uuid))
								{
									if (target.huntedClass().isPresent())
										return target.huntedClass().get().equals(PlayerClassManager.getClassFor(context.target()).id());
									else if (target.huntedClassType().isPresent())
										return target.huntedClassType().get().equals(PlayerClassManager.getClassFor(context.target()).getType());
									else
										return true;
								}
							}
						}
					}
				}
				return false;
			}
		};
		
		private final String id;
		private final Trigger.Criteria criteria;
		
		private Type(String id, Trigger.Criteria criteria)
		{
			this.id = id;
			this.criteria = criteria;
		}
		
		@Override
		public String getSerializedName()
		{
			return this.id;
		}
		
		public Trigger.Criteria getCriteria()
		{
			return this.criteria;
		}
		
		protected abstract TargetCriteria make(Optional<ResourceLocation> huntedClass, Optional<HuntedClassType> type);
		
		protected abstract boolean matches(TargetCriteria target, TriggerContext context);
	}
}
