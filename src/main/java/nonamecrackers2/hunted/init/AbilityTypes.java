package nonamecrackers2.hunted.init;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.ability.type.AbilityType;
import nonamecrackers2.hunted.ability.type.ApplyEffect;
import nonamecrackers2.hunted.ability.type.Bind;
import nonamecrackers2.hunted.ability.type.BlinkTeleport;
import nonamecrackers2.hunted.ability.type.Eliminate;
import nonamecrackers2.hunted.ability.type.GiveItems;
import nonamecrackers2.hunted.ability.type.ModifyCooldown;
import nonamecrackers2.hunted.ability.type.TargetTeleport;
import nonamecrackers2.hunted.ability.type.Teleport;
import nonamecrackers2.hunted.ability.type.TextDisplay;
import nonamecrackers2.hunted.ability.type.ToggleAbilities;
import nonamecrackers2.hunted.registry.HuntedRegistries;

public class AbilityTypes 
{
	private static final DeferredRegister<AbilityType<?>> ABILITY_TYPES = DeferredRegister.create(HuntedRegistries.ABILITY_TYPES_NAME, HuntedMod.MOD_ID);
	
	public static final RegistryObject<Teleport> TELEPORT = ABILITY_TYPES.register("teleport", Teleport::new);
	public static final RegistryObject<Eliminate> ELIMINATE = ABILITY_TYPES.register("eliminate", Eliminate::new);
	public static final RegistryObject<GiveItems> GIVE_ITEMS = ABILITY_TYPES.register("give_items", GiveItems::new);
	public static final RegistryObject<ApplyEffect> APPLY_EFFECT = ABILITY_TYPES.register("apply_effect", ApplyEffect::new);
	public static final RegistryObject<BlinkTeleport> BLINK_TELEPORT = ABILITY_TYPES.register("blink_teleport", BlinkTeleport::new);
	public static final RegistryObject<TargetTeleport> TARGET_TELEPORT = ABILITY_TYPES.register("target_teleport", TargetTeleport::new);
	public static final RegistryObject<Bind> BIND = ABILITY_TYPES.register("bind", Bind::new);
	public static final RegistryObject<ToggleAbilities> TOGGLE_ABILITIES = ABILITY_TYPES.register("toggle_abilities", ToggleAbilities::new);
	public static final RegistryObject<ModifyCooldown> MODIFY_COOLDOWN = ABILITY_TYPES.register("modify_cooldown", ModifyCooldown::new);
	public static final RegistryObject<TextDisplay> TEXT_DISPLAY = ABILITY_TYPES.register("text_display", TextDisplay::new);
	
	public static void register(IEventBus modBus)
	{
		ABILITY_TYPES.register(modBus);
	}
}
