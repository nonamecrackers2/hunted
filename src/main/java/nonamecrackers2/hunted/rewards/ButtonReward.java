package nonamecrackers2.hunted.rewards;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
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
	private final Component name;
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
	
	public ButtonReward(ResourceLocation id, Component name, @Nullable Component globalMessage, TargetSupplier globalMessageSupplier, List<Component> rewardMessage, boolean randomMessage, SoundEvent sound, float pitch, List<NamedItemHolder> rewards, List<MobEffectHolder> effects, TargetSupplier supplier, List<String> events)
	{
		this.id = id;
		this.name = name;
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
	
	public void toPacket(FriendlyByteBuf buffer)
	{
		buffer.writeResourceLocation(this.id);
		buffer.writeComponent(this.name);
		buffer.writeNullable(this.globalMessage, FriendlyByteBuf::writeComponent);
		buffer.writeCollection(this.rewardMessage, FriendlyByteBuf::writeComponent);
		buffer.writeBoolean(this.randomMessage);
		buffer.writeRegistryId(ForgeRegistries.SOUND_EVENTS, this.sound);
		buffer.writeFloat(this.pitch);
		buffer.writeCollection(this.rewards, (buf, item) -> item.toPacket(buf));
		buffer.writeCollection(this.effects, (buf, effect) -> effect.toPacket(buf));
	}
	
	public static ButtonReward fromPacket(FriendlyByteBuf buffer)
	{
		ResourceLocation id = buffer.readResourceLocation();
		Component name = buffer.readComponent();
		Component globalMessage = buffer.readNullable(FriendlyByteBuf::readComponent);
		List<Component> rewardMessages = ImmutableList.copyOf(buffer.readList(FriendlyByteBuf::readComponent));
		boolean randomMessage = buffer.readBoolean();
		SoundEvent sound = buffer.readRegistryId();
		float pitch = buffer.readFloat();
		List<NamedItemHolder> rewards = ImmutableList.copyOf(buffer.readList(NamedItemHolder::fromPacket));
		List<MobEffectHolder> effects = ImmutableList.copyOf(buffer.readList(MobEffectHolder::fromPacket));
		return new ButtonReward(id, name, globalMessage, TargetSupplier.DEFAULT, rewardMessages, randomMessage, sound, pitch, rewards, effects, TargetSupplier.DEFAULT, ImmutableList.of());
	}
	
	public void reward(TriggerContext context)
	{
		if (this.globalMessage != null)
			this.globalMessageSupplier.getPlayers(context).forEach(active -> active.sendSystemMessage(HuntedUtil.appendArgs(this.globalMessage, context.player().getDisplayName())));
		for (LivingEntity player : this.supplier.getPlayers(context, false))
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
		for (LivingEntity player : this.supplier.getPlayers(context))
			this.effects.forEach(holder -> player.addEffect(holder.createInstance()));
		if (context.player() instanceof ServerPlayer serverPlayer)
			serverPlayer.playNotifySound(this.sound, SoundSource.PLAYERS, 100.0F, this.pitch);
		this.rewards.stream().collect(Collectors.mapping(item -> {
			ItemStack stack = item.toStack();
			CompoundTag extra = new CompoundTag();
			extra.putBoolean("IsRewardItem", true);
			extra.putString("Reward", this.id.toString());
			stack.addTagElement("HuntedGameData", extra);
			stack.getTag().putBoolean("Unbreakable", true);
			return stack;
		}, Collectors.toList())).forEach(item -> 
		{
			Container inv = HuntedUtil.getInventoryFor(context.player());
			if (inv != null)
				HuntedUtil.addItem(inv, item);
		});
	}
	
	public ResourceLocation getId()
	{
		return this.id;
	}
	
	public Component getName()
	{
		return this.name;
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
