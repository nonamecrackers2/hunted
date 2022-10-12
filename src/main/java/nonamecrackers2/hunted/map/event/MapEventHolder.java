package nonamecrackers2.hunted.map.event;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.init.TriggerTypes;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.trigger.Triggerable;

public record MapEventHolder(ResourceLocation id, Trigger.ConfiguredTrigger<?> trigger, List<MapEvent.ConfiguredMapEvent<?>> events) implements Triggerable
{
	public void use(TriggerContext context)
	{
		if (this.triggerCriteria().matches(context) && this.trigger.matches(context))
		{
			this.events.forEach(event -> event.use(context, this.getTag(context.getGame())));
			context.getGame().trigger(TriggerTypes.EVENT.get(), TriggerContext.builder().event(this));
		}
	}
	
	public void begin(ServerLevel level, HuntedGame game)
	{
		this.events.forEach(event -> event.begin(level, game, this.getTag(game)));
	}
	
	public void tick(ServerLevel level, HuntedGame game)
	{
		this.events.forEach(event -> event.tick(level, game, this.getTag(game)));
	}
	
	public void reset(ServerLevel level, HuntedGame game)
	{
		this.events.forEach(event -> event.reset(level, game, this.getTag(game)));
	}
	
	@Override
	public Trigger.Criteria triggerCriteria()
	{
		Trigger.Criteria criteria = Trigger.criteria();
		for (var event : this.events)
			criteria.combine(event.triggerCriteria());
		return criteria;
	}
	
	private CompoundTag getTag(HuntedGame game)
	{
		return game.getOrCreateTagElement(this.id().toString());
	}
}
