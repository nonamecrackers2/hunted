package nonamecrackers2.hunted.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.block.BearTrapBlock;
import nonamecrackers2.hunted.block.KeyholeBlock;
import nonamecrackers2.hunted.block.KioskBlock;
import nonamecrackers2.hunted.block.RewardButtonBlock;

public class HuntedBlocks
{
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HuntedMod.MOD_ID);
	
	public static final RegistryObject<KeyholeBlock> KEYHOLE = BLOCKS.register("keyhole", () -> new KeyholeBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE).strength(2.0F, 1200.0F)));
	public static final RegistryObject<KioskBlock> KIOSK = BLOCKS.register("kiosk", () -> new KioskBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD).lightLevel(state -> {
		return 10;
	})));
	public static final RegistryObject<RewardButtonBlock> REWARD_BUTTON = BLOCKS.register("reward_button", () -> new RewardButtonBlock(BlockBehaviour.Properties.of(Material.DECORATION).noCollission().strength(1.0F, 1200.0F)));
	public static final RegistryObject<BearTrapBlock> BEAR_TRAP = BLOCKS.register("bear_trap", () -> new BearTrapBlock(BlockBehaviour.Properties.of(Material.METAL).noCollission().strength(5.0F, 6.0F).requiresCorrectToolForDrops()));
	
	public static void register(IEventBus modBus)
	{
		BLOCKS.register(modBus);
	}
}
