package nonamecrackers2.hunted.capability;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;

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
	
	public static @Nullable HuntedClass getClassFor(LivingEntity entity)
	{
		PlayerClassManager manager = entity.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
		if (manager != null)
			return manager.getCurrentClass().orElse(null);
		else
			return null;
	}
}
