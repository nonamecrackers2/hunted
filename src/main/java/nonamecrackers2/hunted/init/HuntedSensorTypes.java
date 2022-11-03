package nonamecrackers2.hunted.init;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.entity.ai.sensing.HunterEntitySensor;
import nonamecrackers2.hunted.entity.ai.sensing.NearestGlowingEntitySensor;

public class HuntedSensorTypes
{
	private static final DeferredRegister<SensorType<?>> SENSORS_TYPES = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, HuntedMod.MOD_ID);
	
	public static final RegistryObject<SensorType<HunterEntitySensor>> HUNTER_ENTITY_SENSOR = SENSORS_TYPES.register("hunter_entity_sensor", () -> new SensorType<>(HunterEntitySensor::new));
	public static final RegistryObject<SensorType<NearestGlowingEntitySensor<LivingEntity>>> NEAREST_GLOWING_ENTITY = SENSORS_TYPES.register("nearest_glowing_entity", () -> new SensorType<>(NearestGlowingEntitySensor::new));
	
	public static void register(IEventBus modBus)
	{
		SENSORS_TYPES.register(modBus);
	}
}
