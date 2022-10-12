package nonamecrackers2.hunted.ability.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.TargetSupplier;

public class Teleport extends AbilityType<Teleport.Settings>
{
	private static final Codec<Teleport.Settings> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(Codec.DOUBLE.fieldOf("distance").forGetter(Settings::distance)).apply(instance, Settings::new);
	});
	
	public Teleport()
	{
		super(CODEC, false);
	}

	@Override
	public AbilityType.Result use(Teleport.Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier) 
	{
		float y = (context.player().getYRot() + 90.0F) * ((float)Math.PI / 180.0F);
		Vec3 vec = new Vec3(Mth.cos(y), 0.0D, Mth.sin(y)).scale(settings.distance).add(context.player().position());
		ClipContext clipContext = new ClipContext(context.player().position(), vec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, context.player());
		BlockHitResult ray = context.level().clip(clipContext);
		Vec3 pos = Vec3.atBottomCenterOf(ray.getBlockPos().relative(ray.getDirection()));
		context.player().teleportTo(pos.x, pos.y, pos.z);
		return AbilityType.Result.SUCCESS;
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(Teleport.Settings settings)
	{
		return Trigger.criteria().player();
	}
	
	protected static record Settings(double distance) {}
}
