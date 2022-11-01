package nonamecrackers2.hunted.ability.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.TargetSupplier;

public class TextDisplay extends AbilityType<TextDisplay.Settings>
{
	public TextDisplay()
	{
		super(null);
	}
	
	@Override
	public AbilityType.Result use(TextDisplay.Settings settings, TriggerContext context, CompoundTag tag, TargetSupplier supplier)
	{
		for (LivingEntity player : settings.supplier().getPlayers(context, false))
			player.sendSystemMessage(settings.text());
		return AbilityType.Result.PASS;
	}

	@Override
	public Trigger.Criteria triggerCriteria(TextDisplay.Settings settings)
	{
		return settings.supplier().getTriggerCriteria();
	}
	
	@Override
	public AbilityType.ConfiguredAbilityType<TextDisplay.Settings> configure(JsonElement element)
	{
		JsonObject object = GsonHelper.convertToJsonObject(element, "settings");
		return new AbilityType.ConfiguredAbilityType<>(this, read(object), TargetSupplier.CODEC.parse(JsonOps.INSTANCE, object.get("target").deepCopy()).resultOrPartial(HuntedUtil::throwJSE).orElse(TargetSupplier.DEFAULT));
	}
	
	public static TextDisplay.Settings read(JsonObject object)
	{
		Component text = Component.Serializer.fromJson(object.get("text"));
		if (text == null)
			throw new JsonSyntaxException("Invalid text!");
		TargetSupplier supplier = TargetSupplier.DEFAULT;
		if (object.has("target"))
			supplier = TargetSupplier.CODEC.parse(JsonOps.INSTANCE, object.get("target")).resultOrPartial(HuntedUtil::throwJSE).get();
		return new TextDisplay.Settings(supplier, text);
	}
	
	public static record Settings(TargetSupplier supplier, Component text) {}
}
