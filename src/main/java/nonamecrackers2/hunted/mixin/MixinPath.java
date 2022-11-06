package nonamecrackers2.hunted.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Target;

@Mixin(Path.class)
public interface MixinPath
{
	@Invoker
	void callSetDebug(Node[] open, Node[] closed, Set<Target> targets);
}
