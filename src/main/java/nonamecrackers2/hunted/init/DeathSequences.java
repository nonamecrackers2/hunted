package nonamecrackers2.hunted.init;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.death.DeathSequence;
import nonamecrackers2.hunted.death.GenericDeathSequence;
import nonamecrackers2.hunted.death.ReviveDeathSequence;
import nonamecrackers2.hunted.registry.HuntedRegistries;

public class DeathSequences
{
	private static final DeferredRegister<DeathSequence<?>> DEATH_SEQUENCES = DeferredRegister.create(HuntedRegistries.DEATH_SEQUENCES_NAME, HuntedMod.MOD_ID);
	
	public static final RegistryObject<GenericDeathSequence> GENERIC = DEATH_SEQUENCES.register("generic", GenericDeathSequence::new);
	public static final RegistryObject<ReviveDeathSequence> REVIVE = DEATH_SEQUENCES.register("revive", ReviveDeathSequence::new);
	
	public static void register(IEventBus modBus)
	{
		DEATH_SEQUENCES.register(modBus);
	}
}
