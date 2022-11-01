package nonamecrackers2.hunted.trigger.type;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.ability.type.Bind;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.capability.ServerPlayerClassManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

@Deprecated
public class EliminatedTrigger extends Trigger<EliminatedTrigger.Settings>
{
	public static final Codec<EliminatedTrigger.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(Codec.BOOL.optionalFieldOf("binded_eliminated").forGetter(s -> Optional.of(s.isBinded))).apply(instance, b -> new EliminatedTrigger.Settings(b.orElse(false)));
	});
	
	public EliminatedTrigger()
	{
		super(CODEC, Trigger.criteria().player().target());
	}
	
	@Override
	public boolean matches(EliminatedTrigger.Settings settings, TriggerContext context)
	{
		if (super.matches(settings, context))
		{
			if (settings.isBinded())
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
									return true;
							}
						}
					}
				}
				return false;
			}
			else
			{
				return true;
			}
		}
		else
		{
			return false;
		}
	}
	
	@Deprecated
	public static record Settings(boolean isBinded) {}
}
