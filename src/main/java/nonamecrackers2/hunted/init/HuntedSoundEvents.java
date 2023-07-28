package nonamecrackers2.hunted.init;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;

public class HuntedSoundEvents
{
	private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HuntedMod.MOD_ID);
	
	public static final RegistryObject<SoundEvent> HEARTBEAT = create("heartbeat");
	public static final RegistryObject<SoundEvent> JUMPSCARE = create("jumpscare");
	public static final RegistryObject<SoundEvent> CREAK = create("creak");
	public static final RegistryObject<SoundEvent> MANSION_AMBIENCE = create("mansion_ambience");
	public static final RegistryObject<SoundEvent> HUNTER_AMBIENCE = create("hunter_ambience");
	
	private static RegistryObject<SoundEvent> create(String id)
	{
		return SOUND_EVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(HuntedMod.resource(id)));
	}
	
	public static void register(IEventBus modBus)
	{
		SOUND_EVENTS.register(modBus);
	}
}
