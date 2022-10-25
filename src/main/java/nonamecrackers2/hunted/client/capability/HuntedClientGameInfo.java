package nonamecrackers2.hunted.client.capability;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;
import nonamecrackers2.hunted.map.HuntedMap;

public class HuntedClientGameInfo
{
	private boolean gameRunning;
	private List<Component> overlay = Lists.newArrayList();
	private Optional<HuntedMap> map = Optional.empty();
	private boolean buttonHighlighting;
	
	public boolean isGameRunning()
	{
		return this.gameRunning;
	}
	
	public void setGameRunning(boolean flag)
	{
		this.gameRunning = flag;
	}
	
	public List<Component> getOverlay()
	{
		return this.overlay;
	}
	
	public void setOverlay(List<Component> text)
	{
		this.overlay = text;
	}
	
	public Optional<HuntedMap> getMap()
	{
		return this.map;
	}
	
	public void setMap(@Nullable HuntedMap map)
	{
		this.map = Optional.ofNullable(map);
	}
	
	public boolean buttonHighlighting()
	{
		return this.buttonHighlighting;
	}
	
	public void setButtonHighlighting(boolean flag)
	{
		this.buttonHighlighting = flag;
	}
}
