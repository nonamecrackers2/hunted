package nonamecrackers2.hunted.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.Vec3;
import nonamecrackers2.hunted.util.NoVibrationSignal;

@Mixin(VibrationListener.class)
public abstract class MixinVibrationListener
{
	@Shadow
	protected VibrationListener.VibrationListenerConfig config;
	
	@Inject(
		method = "scheduleSignal",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"),
		cancellable = true
	)
	public void scheduleSignalHead(ServerLevel level, GameEvent event, GameEvent.Context context, Vec3 pos, Vec3 pos1, CallbackInfo ci)
	{
		if (this.config instanceof NoVibrationSignal)
			ci.cancel();
	}
}
