package nonamecrackers2.hunted.trigger;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.map.event.MapEventHolder;
import nonamecrackers2.hunted.rewards.ButtonReward;

public record TriggerContext(Trigger<?> trigger, ServerLevel level, @Nullable LivingEntity player, @Nullable InteractionHand hand, ItemStack stack, @Nullable BlockHitResult result, @Nullable LivingEntity target, @Nullable Ability ability, @Nullable MapEventHolder event, @Nullable ButtonReward reward)
{
	public HuntedGame getGame()
	{
		return this.level.getCapability(HuntedCapabilities.GAME_MANAGER).orElse(null).getCurrentGame().orElse(null);
	}
	
	public PlayerClassManager getClassManager(LivingEntity player)
	{
		return player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
	}
	
	public @Nullable HuntedClass getHuntedClass(LivingEntity player)
	{
		return this.getClassManager(player).getCurrentClass().orElse(null);
	}
	
	public PlayerClassManager getClassManager()
	{
		return this.getClassManager(this.player);
	}
	
	public @Nullable HuntedClass getHuntedClass()
	{
		return this.getHuntedClass(this.player);
	}
	
//	public ServerPlayerClassManager getTargetClassManager()
//	{
//		return this.getClassManager(this.getTargetServerPlayer());
//	}
//	
//	public @Nullable HuntedClass getTargetHuntedClass()
//	{
//		return this.getHuntedClass(this.getTargetServerPlayer());
//	}
	
	public static TriggerContext.Builder builder()
	{
		return new TriggerContext.Builder();
	}
	
	public static class Builder
	{
		private @Nullable LivingEntity player;
		private @Nullable InteractionHand hand;
		private ItemStack item = ItemStack.EMPTY;
		private @Nullable BlockHitResult result;
		private @Nullable LivingEntity target;
		private @Nullable Ability ability;
		private @Nullable MapEventHolder event;
		private @Nullable ButtonReward reward;
		
		public Builder player(LivingEntity player)
		{
			this.player = player;
			return this;
		}
		
		public Builder hand(InteractionHand hand)
		{
			this.hand = hand;
			return this;
		}
		
		public Builder item(ItemStack stack)
		{
			this.item = stack;
			return this;
		}
		
		public Builder result(BlockHitResult result)
		{
			this.result = result;
			return this;
		}
		
		public Builder target(LivingEntity player)
		{
			this.target = player;
			return this;
		}
		
		public Builder ability(Ability ability)
		{
			this.ability = ability;
			return this;
		}
		
		public Builder event(MapEventHolder event)
		{
			this.event = event;
			return this;
		}
		
		public Builder reward(ButtonReward reward)
		{
			this.reward = reward;
			return this;
		}
		
		public Builder of(TriggerContext context)
		{
			this.player = context.player();
			this.hand = context.hand();
			this.item = context.stack();
			this.result = context.result();
			this.target = context.target();
			this.ability = context.ability();
			this.event = context.event();
			this.reward = context.reward();
			return this;
		}
		
		public TriggerContext build(ServerLevel level, Trigger<?> trigger)
		{
			TriggerContext context = new TriggerContext(trigger, level, this.player, this.hand, this.item, this.result, this.target, this.ability, this.event, this.reward);
			trigger.verifyContext(context);
			return context;
		}
	}
}
