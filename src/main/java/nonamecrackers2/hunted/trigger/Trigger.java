package nonamecrackers2.hunted.trigger;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.util.HuntedUtil;

public abstract class Trigger<T>
{
	private final Codec<T> codec;
	protected final Trigger.Criteria criteria;
	
	protected Trigger(Codec<T> codec, Trigger.Criteria criteria)
	{
		this.codec = codec;
		this.criteria = criteria;
	}
	
	public void verifyContext(TriggerContext context)
	{
		if (context.trigger() != this)
			throw new IllegalArgumentException("Cannot check context for mismatched triggers!");
		Objects.requireNonNull(context.level(), "Supplied level must be present!");
		if (this.criteria.player)
			Objects.requireNonNull(context.player(), "Supplied player must be present!");
		if (this.criteria.hand)
			Objects.requireNonNull(context.hand(), "Supplied hand must be present!");
		if (this.criteria.item)
			Objects.requireNonNull(context.stack(), "Supplied item must be present!");
		if (this.criteria.result)
			Objects.requireNonNull(context.result(), "Supplied block hit result must be present!");
		if (this.criteria.target)
			Objects.requireNonNull(context.target(), "Supplied target must be present!");
		if (this.criteria.ability)
			Objects.requireNonNull(context.ability(), "Supplied ability must be present!");
		if (this.criteria.reward)
			Objects.requireNonNull(context.reward(), "Supplied button reward must be present!");
	}
	
	public boolean matches(T settings, TriggerContext context)
	{
		return this.equals(context.trigger());
	}
	
	public Trigger.Criteria getCriteriaAdditions(T settings)
	{
		return criteria();
	}
	
	public static Trigger.ConfiguredTrigger<?> getTrigger(JsonElement element)
	{
		JsonObject object = null;
		String rawType = null;
		if (GsonHelper.isStringValue(element))
		{
			rawType = GsonHelper.convertToString(element, "type");
		}
		else if (element.isJsonObject())
		{
			object = GsonHelper.convertToJsonObject(element, "trigger");
			rawType = GsonHelper.getAsString(object, "type");
		}
		if (rawType != null)
		{
			Trigger<?> trigger = HuntedRegistries.TRIGGERS.get().getValue(new ResourceLocation(rawType));
			if (trigger == null)
				throw new JsonSyntaxException("Unknown or unsupported trigger '" + rawType + "'");
			var defaultSettings = trigger.defaultSettings();
			if (object != null)
				return trigger.configure(object.get("settings"));
			else if (defaultSettings != null)
				return trigger.defaultConfiguration();
			else
				throw new JsonSyntaxException("Trigger type must be configured!");
		}
		else
		{
			throw new JsonSyntaxException("Could not get raw type from element " + element.toString());
		}
	}
	
	public Trigger.ConfiguredTrigger<T> configure(JsonElement element)
	{
		JsonObject object = GsonHelper.convertToJsonObject(element, "settings");
		TargetCriteria criteria = this.defaultTargetCriteria();
		if (object.has("target"))
			criteria = TargetCriteria.CODEC.parse(JsonOps.INSTANCE, object.get("target")).resultOrPartial(HuntedUtil::throwJSE).get(); 
		return new Trigger.ConfiguredTrigger<>(this, this.codec.parse(JsonOps.INSTANCE, object).resultOrPartial(HuntedUtil::throwJSE).get(), criteria);
	}
	
	public Trigger.ConfiguredTrigger<T> defaultConfiguration()
	{
		return new Trigger.ConfiguredTrigger<>(this, this.defaultSettings(), this.defaultTargetCriteria());
	}
	
	protected @Nullable T defaultSettings()
	{
		return null;
	}
	
	protected TargetCriteria defaultTargetCriteria()
	{
		return TargetCriteria.DEFAULT;
	}
	
	public static Trigger.Criteria criteria()
	{
		return new Trigger.Criteria();
	}
	
	public static class ConfiguredTrigger<T>
	{
		public final Trigger<T> trigger;
		public final T settings;
		public final TargetCriteria criteria;
		
		public ConfiguredTrigger(Trigger<T> trigger, T settings, TargetCriteria criteria)
		{
			this.trigger = trigger;
			this.settings = settings;
			this.criteria = criteria;
		}
		
		public boolean matches(TriggerContext context)
		{
			return this.trigger.matches(this.settings, context) && this.criteria.matches(context);
		}
		
		public Trigger.Criteria getCriteria()
		{
			return this.trigger.criteria.copy().combine(this.trigger.getCriteriaAdditions(this.settings));
		}
		
		public void verify(Triggerable triggerable)
		{
			Trigger.Criteria criteria = triggerable.triggerCriteria();
			if (!criteria.matches(this.getCriteria()))
				throw new IllegalArgumentException("Criteria is not met for " + triggerable.toString() + "! " + this.toString() + " does not match " + criteria.toString());
			if (!this.criteria.getTriggerCriteria().matches(this.getCriteria()))
				throw new IllegalArgumentException("Criteria is not met for " + this.criteria.toString() + "! " + this.toString() + " does not match " + this.criteria.getTriggerCriteria() + ". This may mean that this trigger does not support the supplied target criteria");
		}
		
		@Override
		public String toString()
		{
			return this.getClass().getSimpleName() + "[supports=" + this.getCriteria().toString() + "]";
		}
	}
	
	public static class Criteria
	{
		public boolean player;
		public boolean hand;
		public boolean item;
		public boolean result;
		public boolean target;
		public boolean ability;
		public boolean event;
		public boolean reward;
		
		public Criteria player(boolean flag)
		{
			this.player = flag;
			return this;
		}
		
		public Criteria hand(boolean flag)
		{
			this.hand = flag;
			return this;
		}
		
		public Criteria item(boolean flag)
		{
			this.item = flag;
			return this;
		}
		
		public Criteria hitResult(boolean flag)
		{
			this.result = flag;
			return this;
		}
		
		public Criteria target(boolean flag)
		{
			this.target = flag;
			return this;
		}
		
		public Criteria ability(boolean flag)
		{
			this.ability = flag;
			return this;
		}
		
		public Criteria event(boolean flag)
		{
			this.event = flag;
			return this;
		}
		
		public Criteria reward(boolean flag)
		{
			this.reward = flag;
			return this;
		}
		
		public Criteria player()
		{
			return this.player(true);
		}
		
		public Criteria hand()
		{
			return this.hand(true);
		}
		
		public Criteria item()
		{
			return this.item(true);
		}
		
		public Criteria hitResult()
		{
			return this.hitResult(true);
		}
		
		public Criteria target()
		{
			return this.target(true);
		}
		
		public Criteria ability()
		{
			return this.ability(true);
		}
		
		public Criteria event()
		{
			return this.event(true);
		}
		
		public Criteria reward()
		{
			return this.reward(true);
		}
		
		public Criteria copy()
		{
			var newCriteria = new Criteria();
			newCriteria.player = this.player;
			newCriteria.hand = this.hand;
			newCriteria.item = this.item;
			newCriteria.result = this.result;
			newCriteria.target = this.target;
			newCriteria.ability = this.ability;
			newCriteria.event = this.event;
			newCriteria.reward = this.reward;
			return newCriteria;
		}
		
		public Criteria combine(Criteria other)
		{
			if (other.player)
				this.player();
			if (other.hand)
				this.hand();
			if (other.item)
				this.item();
			if (other.result)
				this.hitResult();
			if (other.target)
				this.target();
			if (other.ability)
				this.ability();
			if (other.event)
				this.event();
			if (other.reward)
				this.reward();
			return this;
		}
		
		public boolean matches(Trigger.Criteria criteria)
		{
			if (this.player && !criteria.player)
				return false;
			if (this.hand && !criteria.hand)
				return false;
			if (this.item && !criteria.item)
				return false;
			if (this.result && !criteria.result)
				return false;
			if (this.target && !criteria.target)
				return false;
			if (this.ability && !criteria.ability)
				return false;
			if (this.event && !criteria.event)
				return false;
			if (this.reward && !criteria.reward)
				return false;
			return true;
		}
		
		public boolean matches(TriggerContext context)
		{
			if (this.player && context.player() == null)
				return false;
			if (this.hand && context.hand() == null)
				return false;
			if (this.item && context.stack() == null)
				return false;
			if (this.result && context.result() == null)
				return false;
			if (this.target && context.target() == null)
				return false;
			if (this.ability && context.ability() == null)
				return false;
			if (this.event && context.event() == null)
				return false;
			if (this.reward && context.reward() == null)
				return false;
			return true;
		}
		
		@Override
		public String toString()
		{
			return "Trigger.Criteria[activationPlayer=" + this.player + ", "
					+ "hand=" + this.hand + ", "
					+ "item=" + this.item + ", "
					+ "blockHitResult=" + this.result + ", "
					+ "target=" + this.target + ", "
					+ "ability=" + this.ability + ", "
					+ "event=" + this.event + ", "
					+ "reward=" + this.reward + "]";
		}
	}
}
