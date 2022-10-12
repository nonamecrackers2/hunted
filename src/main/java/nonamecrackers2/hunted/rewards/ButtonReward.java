package nonamecrackers2.hunted.rewards;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import nonamecrackers2.hunted.map.event.MapEventDataManager;
import nonamecrackers2.hunted.map.event.MapEventHolder;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.HuntedUtil;
import nonamecrackers2.hunted.util.MobEffectHolder;
import nonamecrackers2.hunted.util.NamedItemHolder;
import nonamecrackers2.hunted.util.TargetSupplier;

public class ButtonReward
{
	private final ResourceLocation id;
	private final @Nullable Component globalMessage;
	private final TargetSupplier globalMessageSupplier;
	private final List<Component> rewardMessage;
	private final boolean randomMessage;
	private final SoundEvent sound;
	private final float pitch;
	private final List<NamedItemHolder> rewards;
	private final List<MobEffectHolder> effects;
	private final TargetSupplier supplier;
	private final List<String> events;
	
	public ButtonReward(ResourceLocation id, @Nullable Component globalMessage, TargetSupplier globalMessageSupplier, List<Component> rewardMessage, boolean randomMessage, SoundEvent sound, float pitch, List<NamedItemHolder> rewards, List<MobEffectHolder> effects, TargetSupplier supplier, List<String> events)
	{
		this.id = id;
		this.globalMessage = globalMessage;
		this.globalMessageSupplier = globalMessageSupplier;
		this.rewardMessage = rewardMessage;
		this.randomMessage = randomMessage;
		this.sound = sound;
		this.pitch = pitch;
		this.rewards = rewards;
		this.effects = effects;
		this.supplier = supplier;
		this.events = events;
	}
	
	public void reward(TriggerContext context)
	{
		if (this.globalMessage != null)
			this.globalMessageSupplier.getPlayers(context).forEach(active -> active.sendSystemMessage(HuntedUtil.appendArgs(this.globalMessage, context.player().getDisplayName())));
		for (ServerPlayer player : this.supplier.getPlayers(context, false))
		{
			if (!this.randomMessage)
			{
				this.rewardMessage.forEach(message -> player.sendSystemMessage(message));
			}
			else
			{
				if (this.rewardMessage.size() > 0)
					player.sendSystemMessage(this.rewardMessage.get(player.getRandom().nextInt(this.rewardMessage.size())));
			}
		}
		for (ServerPlayer player : this.supplier.getPlayers(context))
			this.effects.forEach(holder -> player.addEffect(holder.createInstance()));
		context.player().playNotifySound(this.sound, SoundSource.PLAYERS, 100.0F, this.pitch);
		this.rewards.stream().collect(Collectors.mapping(item -> {
			ItemStack stack = item.toStack();
			CompoundTag extra = new CompoundTag();
			extra.putBoolean("IsRewardItem", true);
			extra.putString("Reward", this.id.toString());
			stack.addTagElement("HuntedGameData", extra);
			stack.getTag().putBoolean("Unbreakable", true);
			return stack;
		}, Collectors.toList())).forEach(context.player()::addItem);
	}
	
	public ResourceLocation getId()
	{
		return this.id;
	}
	
	public List<MapEventHolder> getEvents()
	{
		ImmutableList.Builder<MapEventHolder> events = ImmutableList.builder();
		for (String rawId : this.events)
		{
			if (rawId.startsWith("#"))
			{
				rawId = rawId.substring(1);
				for (Map.Entry<ResourceLocation, MapEventHolder> event : MapEventDataManager.INSTANCE.values().entrySet())
				{
					if (event.getKey().toString().indexOf(rawId) == 0)
						events.add(event.getValue());
				}
			}
			else
			{
				ResourceLocation id = new ResourceLocation(rawId);
				MapEventHolder event = MapEventDataManager.INSTANCE.get(id);
				if (event != null)
					events.add(event);
				else
					throw new JsonSyntaxException("Unknown event '" + id + "'");
			}
		}
		return events.build();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		else if (obj instanceof ButtonReward reward)
		{
			if (reward.getId().equals(this.id))
				return true;
			else
				return false;
		}
		else
		{
			return false;
		}
	}
}
