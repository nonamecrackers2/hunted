package nonamecrackers2.hunted.ability.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;

import net.minecraft.util.GsonHelper;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.TargetSupplier;

public abstract class NoSettingsAbilityType extends AbilityType<NoSettingsAbilityType.EmptySettings>
{
	protected NoSettingsAbilityType(boolean supportsSupplier)
	{
		super(null, supportsSupplier);
	}
	
	@Override
	public AbilityType.ConfiguredAbilityType<EmptySettings> configure(JsonElement element)
	{
		JsonObject object = GsonHelper.convertToJsonObject(element, "settings");
		TargetSupplier supplier = this.defaultSupplier();
		if (this.supportsSupplier && object.has("target"))
			supplier = TargetSupplier.CODEC.parse(JsonOps.INSTANCE, object.get("target")).resultOrPartial(HuntedUtil::throwJSE).get();
		else if (object.has("target"))
			throw new JsonSyntaxException("This ability type does not support the target supplier!");
		return new ConfiguredAbilityType<>(this, new NoSettingsAbilityType.EmptySettings(), supplier);
	}
	
	protected static record EmptySettings() {}
}
