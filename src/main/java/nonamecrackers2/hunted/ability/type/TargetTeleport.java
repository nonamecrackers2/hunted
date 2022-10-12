package nonamecrackers2.hunted.ability.type;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import nonamecrackers2.hunted.capability.HuntedClassManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.TargetSupplier;

public class TargetTeleport extends AbilityType<TargetTeleport.Settings>
{
	public static final Codec<TargetTeleport.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(HuntedRegistries.HUNTED_CLASS_TYPES.get().getCodec().optionalFieldOf("class_type").forGetter(TargetTeleport.Settings::type))
				.apply(instance, TargetTeleport.Settings::new);
	});
	
	public TargetTeleport()
	{
		super(CODEC, false);
	}
	
	@Override
	public AbilityType.Result use(TargetTeleport.Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
	{
		ServerPlayer player = context.target();
		HuntedClass huntedClass = HuntedClassManager.getClassForPlayer(player);
		if (huntedClass != null && settings.type.isPresent() ? huntedClass.getType().equals(settings.type.get()) : true && !context.getGame().isPlayerEliminated(player) && !context.getGame().hasPlayerEscaped(player))
		{
			BlockPos pos = player.blockPosition();
			context.player().teleportTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
			return AbilityType.Result.SUCCESS;
		}
		return AbilityType.Result.FAIL;
	}

	@Override
	public Trigger.Criteria triggerCriteria(TargetTeleport.Settings settings)
	{
		return Trigger.criteria().player().target();
	}
	
	public static record Settings(Optional<HuntedClassType> type) {}
}
