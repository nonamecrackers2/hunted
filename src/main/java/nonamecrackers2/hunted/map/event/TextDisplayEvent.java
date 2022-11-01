package nonamecrackers2.hunted.map.event;

import com.google.gson.JsonElement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.ability.type.TextDisplay;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

public class TextDisplayEvent extends MapEvent<TextDisplay.Settings>
{
	public TextDisplayEvent()
	{
		super(null);
	}

	@Override
	public void activate(TextDisplay.Settings settings, TriggerContext context, CompoundTag data)
	{
		for (LivingEntity player : settings.supplier().getPlayers(context, false))
			player.sendSystemMessage(settings.text());
	}

	@Override
	public Trigger.Criteria triggerCriteria(TextDisplay.Settings settings)
	{
		return settings.supplier().getTriggerCriteria();
	}
	
	@Override
	public MapEvent.ConfiguredMapEvent<TextDisplay.Settings> configure(JsonElement element)
	{
		return new MapEvent.ConfiguredMapEvent<>(this, TextDisplay.read(GsonHelper.convertToJsonObject(element, "settings")));
	}
}
