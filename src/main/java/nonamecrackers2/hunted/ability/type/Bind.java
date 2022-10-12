package nonamecrackers2.hunted.ability.type;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.SoundEventHolder;
import nonamecrackers2.hunted.util.TargetSupplier;

public class Bind extends AbilityType<Bind.Settings>
{
	public static final String BINDED = "Player";
	
	public Bind()
	{
		super(null, false);
	}
	
	@Override
	public AbilityType.ConfiguredAbilityType<Bind.Settings> configure(JsonElement element)
	{
		JsonObject object = GsonHelper.convertToJsonObject(element, "masked");
		String rawType = GsonHelper.getAsString(object, "masked");
		AbilityType<?> maskedType = HuntedRegistries.ABILITY_TYPES.get().getValue(new ResourceLocation(rawType));
		if (maskedType == null)
			throw new JsonSyntaxException("Unknown or unsupported ability type '" + rawType + "'");
		AbilityType.ConfiguredAbilityType<?> masked = maskedType.configure(object.get("settings"));
		HuntedClassType type = null;
		if (object.has("class_type"))
		{
			String rawClassType = GsonHelper.getAsString(object, "class_type");
			type = HuntedRegistries.HUNTED_CLASS_TYPES.get().getValue(new ResourceLocation(rawClassType));
			if (type == null)
				throw new JsonSyntaxException("Unknown or unsupported class type '" + rawClassType + "'");
		}
		Optional<SoundEventHolder> bindSound = SoundEventHolder.CODEC.parse(JsonOps.INSTANCE, object.get("bind_sound")).result();
		AbilityType.Result result = AbilityType.Result.SUCCESS;
		if (object.has("result_criteria"))
		{
			String id = GsonHelper.getAsString(object, "result_criteria");
			for (AbilityType.Result resultType : AbilityType.Result.values())
			{
				if (resultType.getSerializedName().equals(id))
				{
					result = resultType;
					id = null;
					break;
				}
			}
			if (id != null)
				throw new JsonSyntaxException("Unknown or unsupported ability result '" + id + "'");
		}
		String tagPath = null;
		if (object.has("bind_tag_path"))
			tagPath = GsonHelper.getAsString(object, "bind_tag_path");
		Component bindMessage = null;
		if (object.has("bind_message"))
			bindMessage = Component.Serializer.fromJson(object.get("bind_message"));
		TargetSupplier supplier = this.defaultSupplier();
		if (object.has("target"))
			supplier = TargetSupplier.CODEC.parse(JsonOps.INSTANCE, object.get("target")).resultOrPartial(HuntedUtil::throwJSE).get();
		return new AbilityType.ConfiguredAbilityType<>(this, new Bind.Settings(masked, Optional.ofNullable(type), bindSound, result, Optional.ofNullable(tagPath), Optional.ofNullable(bindMessage)), supplier);
	}

	@Override
	public AbilityType.Result use(Bind.Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
	{
		CompoundTag bindTag = tag;
		if (settings.tagPath().isPresent())
			bindTag = context.getClassManager().getOrCreateTagElement(settings.tagPath().get());
		
		var result = AbilityType.Result.FAIL;
		
		if (!bindTag.contains(BINDED))
		{
			if (context.target() != null)
			{
				HuntedClass huntedClass = context.getTargetHuntedClass();
				if (settings.bindType().isPresent() ? huntedClass.getType().equals(settings.bindType().get()) : true)
				{
					bindTag.putUUID(BINDED, context.target().getUUID());
					settings.bindSound().ifPresent(sound -> context.player().playNotifySound(sound.event(), SoundSource.PLAYERS, sound.volume(), sound.pitch()));
					result = AbilityType.Result.PASS;
					settings.bindMessage.ifPresent(message -> {
						context.player().sendSystemMessage(HuntedUtil.appendArgs(message, context.target().getDisplayName()));
					});
				}
			}
		}
		else
		{
			result = AbilityType.Result.SUCCESS;
		}
		
		if (result.equals(settings.resultCriteria()))
		{
			ServerPlayer target = null;
			if (bindTag.contains(BINDED))
			{
				target = (ServerPlayer)context.level().getPlayerByUUID(bindTag.getUUID(BINDED));
				if (target != null && context.getGame().isPlayerEliminated(target))
					target = null;
			}
			var builder = TriggerContext.builder().of(context).target(target);
			if (settings.type().triggerCriteria().target ? target != null : true)
				return settings.type().use(builder.build(context.level(), context.trigger()), tag);
			else
				return AbilityType.Result.FAIL;
		}
		return result;
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(Bind.Settings settings)
	{
		return Trigger.criteria().player().combine(settings.type.triggerCriteria()).target(false);
	}
	
	public static record Settings(AbilityType.ConfiguredAbilityType<?> type, Optional<HuntedClassType> bindType, Optional<SoundEventHolder> bindSound, AbilityType.Result resultCriteria, Optional<String> tagPath, Optional<Component> bindMessage) {}
}
