package nonamecrackers2.hunted.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;

@Mixin(TrapDoorBlock.class)
public interface MixinTrapDoorBlock
{
	@Invoker
	void callPlaySound(@Nullable Player player, Level level, BlockPos pos, boolean opened);
}
