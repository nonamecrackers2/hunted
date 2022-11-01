package nonamecrackers2.hunted.ability.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.TargetSupplier;

public class BlinkTeleport extends AbilityType<BlinkTeleport.Settings>
{
	public static final Codec<BlinkTeleport.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(Codec.INT.fieldOf("interval").forGetter(BlinkTeleport.Settings::saveInterval)).apply(instance, BlinkTeleport.Settings::new);
	});
	
	public BlinkTeleport()
	{
		super(CODEC);
	}

	@Override
	public AbilityType.Result use(BlinkTeleport.Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
	{
		BlockPos pos = context.getGame().getMap().defaultStartPos();
		if (tag.contains("Pos"))
			pos = NbtUtils.readBlockPos(tag.getCompound("Pos"));
		context.player().teleportTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
		return AbilityType.Result.SUCCESS;
	}
	
	@Override
	public void tick(BlinkTeleport.Settings settings, ServerLevel level, HuntedGame game, LivingEntity player, HuntedClass huntedClass, CompoundTag tag, TargetSupplier supplier)
	{
		if (game.getTimeElapsed() % settings.saveInterval == 0)
		{
			tag.put("Pos", NbtUtils.writeBlockPos(player.blockPosition()));
			tag.putFloat("XRot", player.getXRot());
			tag.putFloat("YRot", player.getYRot());
			if (player instanceof ServerPlayer serverPlayer)
				serverPlayer.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 2.0F);
		}
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(BlinkTeleport.Settings settings)
	{
		return Trigger.criteria().player();
	}
	
	public static record Settings(int saveInterval) {}
}
