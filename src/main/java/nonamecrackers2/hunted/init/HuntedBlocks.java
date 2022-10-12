package nonamecrackers2.hunted.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.block.KeyholeBlock;

public class HuntedBlocks
{
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HuntedMod.MOD_ID);
	
	public static final RegistryObject<KeyholeBlock> KEYHOLE = BLOCKS.register("keyhole", () -> new KeyholeBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE).strength(2.0F, 1200.0F)));
	
	public static void register(IEventBus modBus)
	{
		BLOCKS.register(modBus);
	}
}
