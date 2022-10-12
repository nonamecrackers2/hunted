package nonamecrackers2.hunted.ability.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.trigger.Triggerable;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.TargetSupplier;

public abstract class AbilityType<T>
{
	protected final Codec<T> codec;
	protected final boolean supportsSupplier;
	
	public AbilityType(Codec<T> codec, boolean supportsSupplier)
	{
		this.codec = codec;
		this.supportsSupplier = supportsSupplier;
	}
	
	public AbilityType(Codec<T> codec)
	{
		this(codec, true);
	}
	
	public abstract AbilityType.Result use(T settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier);
	
	public abstract Trigger.Criteria triggerCriteria(T settings);
	
	public void reset(T settings, ServerLevel level, HuntedGame game, ServerPlayer player, HuntedClass huntedClass, CompoundTag tag, TargetSupplier supplier) {}
	
	public void tick(T settings, ServerLevel level, HuntedGame game, ServerPlayer player, HuntedClass huntedClass, CompoundTag tag, TargetSupplier supplier) {}
	
	public ConfiguredAbilityType<T> configure(JsonElement element)
	{
		JsonObject object = GsonHelper.convertToJsonObject(element, "settings");
		TargetSupplier supplier = this.defaultSupplier();
		if (this.supportsSupplier && object.has("target"))
			supplier = TargetSupplier.CODEC.parse(JsonOps.INSTANCE, object.get("target")).resultOrPartial(HuntedUtil::throwJSE).get();
		else if (object.has("target"))
			throw new JsonSyntaxException("This ability type does not support the target supplier!");
		return new ConfiguredAbilityType<>(this, this.codec.parse(JsonOps.INSTANCE, object).resultOrPartial(HuntedUtil::throwJSE).get(), supplier);
	}
	
	protected TargetSupplier defaultSupplier()
	{
		return TargetSupplier.DEFAULT;
	}
	
	public static record ConfiguredAbilityType<T>(AbilityType<T> type, T settings, TargetSupplier supplier) implements Triggerable
	{
		public AbilityType.Result use(TriggerContext context, CompoundTag tag)
		{
			return this.type.use(this.settings, context, tag, this.supplier);
		}
		
		@Override
		public Trigger.Criteria triggerCriteria()
		{
			return this.type.triggerCriteria(this.settings).combine(this.supplier.getTriggerCriteria());
		}
		
		public void reset(ServerLevel level, HuntedGame game, ServerPlayer player, HuntedClass huntedClass, CompoundTag tag)
		{
			this.type.reset(this.settings, level, game, player, huntedClass, tag, this.supplier);
		}
		
		public void tick(ServerLevel level, HuntedGame game, ServerPlayer player, HuntedClass huntedClass, CompoundTag tag)
		{
			this.type.tick(this.settings, level, game, player, huntedClass, tag, this.supplier);
		}
	}
	
	public static enum Result implements StringRepresentable
	{
		SUCCESS(0, "success"),
		PASS(1, "pass"),
		FAIL(2, "fail");
		
		private final String id;
		private int order;
		
		private Result(int order, String id)
		{
			this.order = order;
			this.id = id;
		}
		
		@Override
		public String getSerializedName()
		{
			return this.id;
		}
		
		public boolean isMoreSuccessful(Result result)
		{
			return this.order < result.order;
		}
	}
}