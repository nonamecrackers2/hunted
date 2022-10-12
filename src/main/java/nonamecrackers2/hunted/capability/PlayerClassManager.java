package nonamecrackers2.hunted.capability;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.huntedclass.HuntedClass;

public interface PlayerClassManager
{
	Optional<HuntedClass> getCurrentClass();
	
	void setClass(@Nullable HuntedClass huntedClass);
	
	default void copyFrom(PlayerClassManager manager)
	{
		this.setClass(manager.getCurrentClass().orElse(null));
	}
	
	boolean isInGame();
	
	boolean hasEscaped();
	
	@Nullable ResourceLocation getMask();
}
