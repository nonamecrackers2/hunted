package nonamecrackers2.hunted.huntedclass.type;

import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;

public abstract class HuntedClassType
{
	protected final Optional<Integer> limit;
	protected final int color;
	protected final boolean canCollectRewards;
	protected final boolean requiresDeathSequence;
	protected final boolean canEscape;
	protected final boolean isDangerous;
	
	public HuntedClassType(HuntedClassType.Properties properties)
	{
		this.limit = properties.limit;
		this.color = properties.color;
		this.canCollectRewards = properties.canCollectRewards;
		this.requiresDeathSequence = properties.requiresDeathSequence;
		this.canEscape = properties.canEscape;
		this.isDangerous = properties.isDangerous;
	}
	
	public int getColor()
	{
		return this.color;
	}
	
	public boolean canCollectRewards()
	{
		return this.canCollectRewards;
	}
	
	public boolean requiresDeathSequence()
	{
		return this.requiresDeathSequence;
	}
	
	public boolean canEscape()
	{
		return this.canEscape;
	}
	
	public boolean isDangerous()
	{
		return this.isDangerous;
	}
	
	public abstract boolean checkObjective(ServerLevel level, HuntedGame game, LivingEntity player, HuntedClass huntedClass);
	
	public boolean checkPartialObjective(ServerLevel level, HuntedGame game, LivingEntity player, HuntedClass huntedClass)
	{
		return false;
	}
	
	public static class Properties
	{
		private Optional<Integer> limit = Optional.empty();
		private int color = 0xFFFFFF;
		private boolean canCollectRewards = true;
		private boolean requiresDeathSequence = true;
		private boolean canEscape = true;
		private boolean isDangerous = false;
		
		public Properties setLimit(int limit)
		{
			this.limit = Optional.of(limit);
			return this;
		}
		
		public Properties setColor(int color)
		{
			this.color = color;
			return this;
		}
		
		public Properties disableRewardCollecting()
		{
			this.canCollectRewards = false;
			return this;
		}
		
		public Properties derequireDeathSequence()
		{
			this.requiresDeathSequence = false;
			return this;
		}
		
		public Properties cannotEscape()
		{
			this.canEscape = false;
			return this;
		}
		
		public Properties makeDangerous()
		{
			this.isDangerous = true;
			return this;
		}
	}
}
