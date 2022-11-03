package nonamecrackers2.hunted.init;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;

public class HuntedMemoryTypes
{
	private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, HuntedMod.MOD_ID);
	
	public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> NEAREST_GLOWING_ENTITIES = MEMORY_MODULE_TYPES.register("nearby_glowing_entities", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<LivingEntity>> NEAREST_GLOWING_ENTITY = MEMORY_MODULE_TYPES.register("nearest_glowing_entity", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<LivingEntity>> NEAREST_ATTACKABLE_GLOWING_ENTITY = MEMORY_MODULE_TYPES.register("nearest_attackable_glowing_entity", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<GlobalPos>> CURRENT_NODE = MEMORY_MODULE_TYPES.register("current_node", () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));
	
	public static void register(IEventBus modBus)
	{
		MEMORY_MODULE_TYPES.register(modBus);
	}
}
