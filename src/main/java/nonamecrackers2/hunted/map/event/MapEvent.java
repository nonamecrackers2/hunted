package nonamecrackers2.hunted.map.event;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.trigger.Triggerable;
import nonamecrackers2.hunted.util.HuntedUtil;

public abstract class MapEvent<T>
{
	private final Codec<T> codec;
	
	public MapEvent(Codec<T> codec)
	{
		this.codec = codec;
	}
	
	public abstract void activate(T settings, TriggerContext context, CompoundTag data);
	
	public void begin(T settings, ServerLevel level, HuntedGame game, CompoundTag data) {}
	
	public void tick(T settings, ServerLevel level, HuntedGame game, CompoundTag data) {}
	
	public void reset(T settings, ServerLevel level, HuntedGame game, CompoundTag data) {}
	
	public abstract Trigger.Criteria triggerCriteria(T settings);
	
	public ConfiguredMapEvent<T> configure(JsonElement element)
	{
		return new ConfiguredMapEvent<>(this, this.codec.parse(JsonOps.INSTANCE, element).resultOrPartial(HuntedUtil::throwJSE).get());
	}
	
	public static record ConfiguredMapEvent<T>(MapEvent<T> type, T settings) implements Triggerable
	{
		public void use(TriggerContext context, CompoundTag tag)
		{
			if (this.triggerCriteria().matches(context))
				this.type.activate(this.settings, context, tag);
		}
		
		public void begin(ServerLevel level, HuntedGame game, CompoundTag tag)
		{
			this.type.begin(this.settings, level, game, tag);
		}
		
		public void tick(ServerLevel level, HuntedGame game, CompoundTag tag)
		{
			this.type.tick(this.settings, level, game, tag);
		}
		
		public void reset(ServerLevel level, HuntedGame game, CompoundTag tag)
		{
			this.type.reset(this.settings, level, game, tag);
		}

		@Override
		public Trigger.Criteria triggerCriteria()
		{
			return this.type.triggerCriteria(this.settings);
		}
	}
}
