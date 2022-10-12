package nonamecrackers2.hunted.trigger.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.util.GsonHelper;
import nonamecrackers2.hunted.trigger.TargetCriteria;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.util.HuntedUtil;

public class BasicTrigger extends Trigger<BasicTrigger.EmptySettings>
{
	public static final BasicTrigger.EmptySettings SETTINGS = new BasicTrigger.EmptySettings();
	
	public BasicTrigger(Trigger.Criteria criteria)
	{
		super(null, criteria);
	}
	
	@Override
	public Trigger.ConfiguredTrigger<EmptySettings> configure(JsonElement element)
	{
		JsonObject object = GsonHelper.convertToJsonObject(element, "settings");
		TargetCriteria criteria = this.defaultTargetCriteria();
		if (object.has("target"))
			criteria = TargetCriteria.CODEC.parse(JsonOps.INSTANCE, object.get("target")).resultOrPartial(HuntedUtil::throwJSE).get();
		return new Trigger.ConfiguredTrigger<>(this, SETTINGS, criteria);
	}
	
	@Override
	protected BasicTrigger.EmptySettings defaultSettings()
	{
		return SETTINGS;
	}

	public static record EmptySettings() {}
}
