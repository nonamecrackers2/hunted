package nonamecrackers2.hunted.init;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.type.BasicTrigger;
import nonamecrackers2.hunted.trigger.type.EventActivatedTrigger;
import nonamecrackers2.hunted.trigger.type.KeyholeTrigger;
import nonamecrackers2.hunted.trigger.type.MeleeTrigger;
import nonamecrackers2.hunted.trigger.type.RewardTrigger;
import nonamecrackers2.hunted.trigger.type.TimerTrigger;

public class TriggerTypes
{
	private static final DeferredRegister<Trigger<?>> TRIGGERS = DeferredRegister.create(HuntedRegistries.TRIGGERS_NAME, HuntedMod.MOD_ID);
	
	public static final RegistryObject<BasicTrigger> KEYBIND = TRIGGERS.register("keybind", () -> new BasicTrigger(Trigger.criteria().player().hand()));
	public static final RegistryObject<MeleeTrigger> MELEE = TRIGGERS.register("melee", () -> new MeleeTrigger());
	public static final RegistryObject<KeyholeTrigger> KEYHOLE = TRIGGERS.register("keyhole_used", () -> new KeyholeTrigger());
	public static final RegistryObject<TimerTrigger> TIMER = TRIGGERS.register("timer", () -> new TimerTrigger());
	public static final RegistryObject<EventActivatedTrigger> EVENT = TRIGGERS.register("event_activated", () -> new EventActivatedTrigger());
	public static final RegistryObject<BasicTrigger> GAME_BEGIN = TRIGGERS.register("game_begin", () -> new BasicTrigger(Trigger.criteria()));
	public static final RegistryObject<BasicTrigger> NONE = TRIGGERS.register("none", () -> new BasicTrigger(Trigger.criteria()));
	public static final RegistryObject<BasicTrigger> ELIMINATED = TRIGGERS.register("eliminated", () -> new BasicTrigger(Trigger.criteria().player().target()));
	public static final RegistryObject<BasicTrigger> REVIVED = TRIGGERS.register("revived", () -> new BasicTrigger(Trigger.criteria().player().target()));
	public static final RegistryObject<RewardTrigger> REWARDED = TRIGGERS.register("rewarded", () -> new RewardTrigger());
	public static final RegistryObject<BasicTrigger> PLAYER_BEGIN = TRIGGERS.register("player_begin", () -> new BasicTrigger(Trigger.criteria().player()));
	
	public static void register(IEventBus modBus)
	{
		TRIGGERS.register(modBus);
	}
}
