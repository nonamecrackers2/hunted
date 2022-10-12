package nonamecrackers2.hunted.util;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

public record MobEffectHolder(MobEffect effect, int duration, int amplifier, boolean hideParticles)
{
	public static final Codec<MobEffectHolder> CODEC = RecordCodecBuilder.create(instance -> 
	{
		return instance.group(ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(MobEffectHolder::effect), 
				Codec.INT.optionalFieldOf("duration").forGetter(i -> Optional.of(i.duration)),
				Codec.INT.optionalFieldOf("amplifier").forGetter(i -> Optional.of(i.amplifier)),
				Codec.BOOL.optionalFieldOf("hide_particles").forGetter(i -> Optional.of(i.hideParticles)))
				.apply(instance, (effect, duration, amplifier, hideParticles) -> new MobEffectHolder(effect, duration.orElse(600), amplifier.orElse(0), hideParticles.orElse(false)));
	});
	
	public MobEffectInstance createInstance()
	{
		return new MobEffectInstance(this.effect, this.duration, this.amplifier, false, !this.hideParticles);
	}
}
