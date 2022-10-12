package nonamecrackers2.hunted.registry;


import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.ability.type.AbilityType;
import nonamecrackers2.hunted.death.DeathSequence;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.map.event.MapEvent;
import nonamecrackers2.hunted.map.overlay.HuntedOverlay;
import nonamecrackers2.hunted.trigger.Trigger;

public class HuntedRegistries 
{
	public static final ResourceLocation HUNTED_CLASS_TYPES_NAME = HuntedMod.resource("hunted_classes");
	public static final ResourceLocation ABILITY_TYPES_NAME = HuntedMod.resource("ability_types");
	public static final ResourceLocation DEATH_SEQUENCES_NAME = HuntedMod.resource("death_sequences");
	public static final ResourceLocation MAP_EVENTS_NAME = HuntedMod.resource("map_events");
	public static final ResourceLocation TRIGGERS_NAME = HuntedMod.resource("triggers");
	public static final ResourceLocation OVERLAYS_NAME = HuntedMod.resource("overlays");
	
	public static Supplier<IForgeRegistry<HuntedClassType>> HUNTED_CLASS_TYPES;
	public static Supplier<IForgeRegistry<AbilityType<?>>> ABILITY_TYPES;
	public static Supplier<IForgeRegistry<DeathSequence<?>>> DEATH_SEQUENCES;
	public static Supplier<IForgeRegistry<MapEvent<?>>> MAP_EVENTS;
	public static Supplier<IForgeRegistry<Trigger<?>>> TRIGGERS;
	public static Supplier<IForgeRegistry<HuntedOverlay<?>>> OVERLAYS;
	
	public static void registerRegistries(@Nonnull NewRegistryEvent event)
	{
		HUNTED_CLASS_TYPES = event.create((new RegistryBuilder<HuntedClassType>())
				.setName(HUNTED_CLASS_TYPES_NAME));
		
		ABILITY_TYPES = event.create((new RegistryBuilder<AbilityType<?>>())
				.setName(ABILITY_TYPES_NAME));
		
		DEATH_SEQUENCES = event.create((new RegistryBuilder<DeathSequence<?>>())
				.setName(DEATH_SEQUENCES_NAME));
		
		MAP_EVENTS = event.create((new RegistryBuilder<MapEvent<?>>())
				.setName(MAP_EVENTS_NAME));
		
		TRIGGERS = event.create((new RegistryBuilder<Trigger<?>>())
				.setName(TRIGGERS_NAME));
		
		OVERLAYS = event.create((new RegistryBuilder<HuntedOverlay<?>>())
				.setName(OVERLAYS_NAME));
	}
}
