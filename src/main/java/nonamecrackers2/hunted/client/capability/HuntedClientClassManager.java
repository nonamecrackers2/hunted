package nonamecrackers2.hunted.client.capability;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;

public class HuntedClientClassManager implements PlayerClassManager
{	
	private final AbstractClientPlayer player;
	private @Nullable ResourceLocation mask;
	private boolean isCurrentlyInGame;
	private boolean hasEscaped;
	private Optional<HuntedClass> huntedClass = Optional.empty();
	
	public HuntedClientClassManager(AbstractClientPlayer player)
	{
		this.player = player;
	}
	
	@Override
	public void copyFrom(PlayerClassManager manager)
	{
		this.isCurrentlyInGame = manager.isInGame();
		this.hasEscaped = manager.hasEscaped();
	}
	
	public void setGameRunning(boolean running)
	{
		this.isCurrentlyInGame = running;
	}
	
	@Override
	public boolean isInGame()
	{
		return this.isCurrentlyInGame;
	}
	
	public void setHasEscaped(boolean escaped)
	{
		this.hasEscaped = escaped;
	}
	
	@Override
	public boolean hasEscaped()
	{
		return this.hasEscaped;
	}
	
	@Override
	public void setClass(HuntedClass huntedClass)
	{
		this.huntedClass = Optional.ofNullable(huntedClass);
	}
	
	@Override
	public Optional<HuntedClass> getCurrentClass()
	{
		return this.huntedClass;
	}
	
	public void setMask(@Nullable ResourceLocation id)
	{
		this.mask = id;
	}
	
	@Override
	public @Nullable ResourceLocation getMask()
	{
		return this.mask;
	}
}
