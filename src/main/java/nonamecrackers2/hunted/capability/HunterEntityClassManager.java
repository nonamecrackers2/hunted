package nonamecrackers2.hunted.capability;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.entity.HunterEntity;
import nonamecrackers2.hunted.huntedclass.HuntedClass;

public class HunterEntityClassManager implements PlayerClassManager
{
	private final HunterEntity entity;
	private final HuntedClass huntedClass;
	
	public HunterEntityClassManager(HunterEntity hunter, HuntedClass huntedClass)
	{
		this.entity = hunter;
		this.huntedClass = huntedClass;
	}
	
	@Override
	public Optional<HuntedClass> getCurrentClass()
	{
		return Optional.of(this.huntedClass);
	}

	@Override
	public void setClass(HuntedClass huntedClass) {}

	@Override
	public boolean isInGame()
	{
		return this.entity.isInGame();
	}

	@Override
	public boolean hasEscaped()
	{
		return this.entity.hasEscaped();
	}

	@Override
	public ResourceLocation getMask()
	{
		return this.entity.getMask();
	}
}
