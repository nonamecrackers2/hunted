package nonamecrackers2.hunted.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import nonamecrackers2.hunted.util.NoVibrationSignal;

@Mixin(VibrationListener.class)
public abstract class MixinVibrationListener
{
	@Shadow
	protected VibrationListener.VibrationListenerConfig config;
	
	@Redirect(
		method = "lambda$tick$8",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I")
	)
	public int sendParticlesRedirect(ServerLevel level, ParticleOptions options, double x, double y, double z, int i, double dx, double dy, double dz, double d)
	{
		if (!(this.config instanceof NoVibrationSignal))
			return level.sendParticles(options, x, y, z, i, dx, dy, dz, d);
		else
			return 0;
	}
}
