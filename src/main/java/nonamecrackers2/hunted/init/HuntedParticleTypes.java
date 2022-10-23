package nonamecrackers2.hunted.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;

public class HuntedParticleTypes
{
	private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, HuntedMod.MOD_ID);
	
	public static final RegistryObject<SimpleParticleType> HIGHLIGHT = PARTICLE_TYPES.register("highlight", () -> new SimpleParticleType(true));
	
	public static void register(IEventBus modBus)
	{
		PARTICLE_TYPES.register(modBus);
	}
}
