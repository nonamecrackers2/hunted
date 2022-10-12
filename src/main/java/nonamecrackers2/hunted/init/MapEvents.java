package nonamecrackers2.hunted.init;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.map.event.GateEvent;
import nonamecrackers2.hunted.map.event.MapEvent;
import nonamecrackers2.hunted.map.event.ModifyAbilityCooldownsEvent;
import nonamecrackers2.hunted.map.event.PlaySoundEvent;
import nonamecrackers2.hunted.map.event.TeleportEvent;
import nonamecrackers2.hunted.map.event.TextDisplayEvent;
import nonamecrackers2.hunted.map.event.ToggleAbilitiesEvent;
import nonamecrackers2.hunted.registry.HuntedRegistries;

public class MapEvents
{
	private static final DeferredRegister<MapEvent<?>> MAP_EVENTS = DeferredRegister.create(HuntedRegistries.MAP_EVENTS_NAME, HuntedMod.MOD_ID);
	
	public static final RegistryObject<GateEvent> GATE = MAP_EVENTS.register("gate", GateEvent::new);
	public static final RegistryObject<TextDisplayEvent> TEXT_DISPLAY = MAP_EVENTS.register("text_display", TextDisplayEvent::new);
	public static final RegistryObject<ToggleAbilitiesEvent> TOGGLE_ABILITIES = MAP_EVENTS.register("toggle_abilities", ToggleAbilitiesEvent::new);
	public static final RegistryObject<ModifyAbilityCooldownsEvent> MODIFY_ABILITY_COOLDOWNS = MAP_EVENTS.register("modify_ability_cooldowns", ModifyAbilityCooldownsEvent::new);
	public static final RegistryObject<TeleportEvent> TELEPORT = MAP_EVENTS.register("map_teleport", TeleportEvent::new);
	public static final RegistryObject<PlaySoundEvent> PLAYSOUND = MAP_EVENTS.register("playsound", PlaySoundEvent::new);
	
	public static void register(IEventBus modBus)
	{
		MAP_EVENTS.register(modBus);
	}
}
