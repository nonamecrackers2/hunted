package nonamecrackers2.hunted.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.block.entity.KeyholeBlockEntity;
import nonamecrackers2.hunted.block.entity.KioskBlockEntity;

public class HuntedBlockEntityTypes
{
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HuntedMod.MOD_ID);
	
	public static final RegistryObject<BlockEntityType<KeyholeBlockEntity>> KEYHOLE = BLOCK_ENTITY_TYPES.register("keyhole", () -> BlockEntityType.Builder.of(KeyholeBlockEntity::new, HuntedBlocks.KEYHOLE.get()).build(null));
	public static final RegistryObject<BlockEntityType<KioskBlockEntity>> KIOSK = BLOCK_ENTITY_TYPES.register("kiosk", () -> BlockEntityType.Builder.of(KioskBlockEntity::new, HuntedBlocks.KIOSK.get()).build(null));
	
	public static void register(IEventBus modBus)
	{
		BLOCK_ENTITY_TYPES.register(modBus);
	}
}
