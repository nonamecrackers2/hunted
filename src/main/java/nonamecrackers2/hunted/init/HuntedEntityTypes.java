package nonamecrackers2.hunted.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.entity.HunterEntity;

public class HuntedEntityTypes
{
	private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HuntedMod.MOD_ID);
	
	public static final RegistryObject<EntityType<HunterEntity>> HUNTER = register("hunter", EntityType.Builder.of(HunterEntity::new, MobCategory.MISC).clientTrackingRange(32).fireImmune().sized(0.6F, 0.8F));
	
	private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, EntityType.Builder<T> builder)
	{
		return ENTITY_TYPES.register(id, () -> builder.build(new ResourceLocation(HuntedMod.MOD_ID, id).toString()));
	}
	
	public static void addEntityAttributes(EntityAttributeCreationEvent event)
	{
		event.put(HUNTER.get(), HunterEntity.createAttributes().build());
	}		
			
	public static void register(IEventBus modBus)
	{
		ENTITY_TYPES.register(modBus);
	}
}
