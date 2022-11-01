package nonamecrackers2.hunted.packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

public class ActivateTriggerPacket extends Packet
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private InteractionHand hand;
	private Trigger<?> trigger;
	private int pickId;
	
	public ActivateTriggerPacket(InteractionHand hand, Trigger<?> trigger, int pickId)
	{
		super(true);
		this.hand = hand;
		this.trigger = trigger;
		this.pickId = pickId;
	}
	
	public ActivateTriggerPacket()
	{
		super(false);
	}

	@Override
	public void decode(FriendlyByteBuf buffer) throws IllegalArgumentException, IndexOutOfBoundsException
	{
		this.hand = buffer.readEnum(InteractionHand.class);
		this.trigger = buffer.readRegistryId();
		this.pickId = buffer.readVarInt();
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		super.encode(buffer);
		buffer.writeEnum(this.hand);
		buffer.writeRegistryId(HuntedRegistries.TRIGGERS.get(), this.trigger);
		buffer.writeVarInt(this.pickId);
	}
	
	@Override
	public Runnable getProcessor(NetworkEvent.Context context)
	{
		return () -> 
		{
			ServerPlayer player = context.getSender();
			player.getLevel().getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(gameManager -> 
			{
				gameManager.getCurrentGame().ifPresent(game -> 
				{
					TriggerContext.Builder triggerContext = TriggerContext.builder().player(player).hand(this.hand);
					Entity entity = player.getLevel().getEntity(this.pickId);
					if (entity instanceof LivingEntity target && game.isActive(target))
						triggerContext.target(target);
					try 
					{
						game.trigger(this.trigger, triggerContext);
					}
					catch (NullPointerException e) 
					{
						LOGGER.error("Error trying to process trigger {}", this.trigger);
						e.printStackTrace();
					}
//					
//					
//					player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
//					{
//						if (manager instanceof ServerPlayerClassManager serverManager)
//						{
//							Entity entity = player.getLevel().getEntity(this.pickId);
//							if (entity instanceof Player targetPlayer && player.hasLineOfSight(targetPlayer))
//							{
//								if (targetPlayer.distanceTo(player) <= 20.0D)
//								{
//									serverManager.useAbility(game, this.hand, this.activation, targetPlayer);
//								}
//								else
//								{
//									LOGGER.warn("Player {} is attempting to reach player {} blocks away!", player, entity.distanceTo(player));
//									serverManager.useAbility(game, this.hand, this.activation, null);
//								}
//							}
//							else
//							{
//								serverManager.useAbility(game, this.hand, this.activation, null);
//							}
//						}
//					});
				});
			});
		};
	}
	
	@Override
	public String toString()
	{
		return "ActivateAbilityMessage[" +
				"hand: " + this.hand + "]";
	}
}
