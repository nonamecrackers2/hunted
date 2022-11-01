package nonamecrackers2.hunted.event;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.capability.HuntedClassManager;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.capability.ServerPlayerClassManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.TriggerTypes;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.HuntedNameFormatting;
import nonamecrackers2.hunted.util.HuntedUtil;

public class HuntedEvents
{
	@SubscribeEvent
	public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		Level level = event.getLevel();
		if (!level.isClientSide)
		{
			BlockPos pos = event.getHitVec().getBlockPos();
			BlockState state = level.getBlockState(pos);
			if (state.hasProperty(ButtonBlock.POWERED) && !state.getValue(ButtonBlock.POWERED))
				level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> manager.getCurrentGame().ifPresent(game -> game.processButton((ServerPlayer)event.getEntity(), pos)));
		}
		event.getEntity().getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (manager.isInGame() && !event.getItemStack().isEmpty())
			{
				ItemStack stack = event.getItemStack();
				if (stack.getOrCreateTag().contains("HuntedGameData"))
				{
					CompoundTag tag = stack.getTagElement("HuntedGameData");
					if (tag.contains("Ability") || tag.contains("IsGivenItem"))
					{
						event.setCancellationResult(InteractionResult.PASS);
						event.setCanceled(true);
					}
				}
			}
		});
	}
	
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event)
	{
		if (event.isWasDeath())
		{
			Player original = event.getOriginal();
			Player player = event.getEntity();
			original.reviveCaps();
			PlayerClassManager originalManager = original.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
			if (originalManager != null)
				player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> manager.copyFrom(originalManager));
			original.invalidateCaps();
		}
	}
	
	@SubscribeEvent
	public static void onTossItem(ItemTossEvent event)
	{
		if (!HuntedUtil.canDrop(event.getEntity().getItem(), event.getPlayer()))
			event.setCanceled(true);
	}
	
	@SubscribeEvent 
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		ServerPlayer player = (ServerPlayer)event.getEntity();
		update(player);
		player.getLevel().getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager ->
		{
			if (!manager.isGameRunning())
			{
				player.moveTo(Vec3.atBottomCenterOf(player.getLevel().getSharedSpawnPos()));
				player.removeAllEffects();
				player.setGameMode(GameType.SURVIVAL);
			}
			manager.updateGameMenus(PacketDistributor.DIMENSION.with(() -> player.level.dimension()));
		});
	}
	
	@SubscribeEvent
	public static void onPlayerChangeDimensions(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		ServerPlayer player = (ServerPlayer)event.getEntity();
		update(player);
		player.getLevel().getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> {
			manager.updateGameMenus(PacketDistributor.DIMENSION.with(() -> player.level.dimension()));
		});
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		update((ServerPlayer)event.getEntity());
	}
	
	@SubscribeEvent
	public static void onPlayerTrackEntity(PlayerEvent.StartTracking event)
	{
		Entity entity = event.getTarget();
		if (entity instanceof ServerPlayer player)
			update(player);
	}
	
	private static void update(ServerPlayer player)
	{
		player.getLevel().getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
		{
			manager.update(player);
			if (!manager.isGameRunning())
			{
				player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(classManager -> 
				{
					if (classManager instanceof ServerPlayerClassManager serverManager)
						serverManager.finish();
					player.refreshDisplayName();
					player.refreshTabListName();
				});
			}
		});
	}
	
	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event)
	{
		Player player = event.getEntity();
		Entity entity = event.getTarget();
		player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (manager.isInGame())
			{
				event.setCanceled(true);
//				manager.getCurrentClass().ifPresent(huntedClass -> 
//				{
//					if (!huntedClass.getType().canAttack())
//					{
//						event.setCanceled(true);
//					}
//					else if (!huntedClass.getType().friendlyFire() && entity instanceof Player target)
//					{
//						target.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(targetManager -> 
//						{
//							if (targetManager.isInGame())
//							{
//								targetManager.getCurrentClass().ifPresent(targetClass -> 
//								{
//									if (targetClass.getType().equals(huntedClass.getType()))
//										event.setCanceled(true);
//								});
//							}
//						});
//					}
//				});
			}
		});
		
		if (!player.level.isClientSide)
		{
			player.getLevel().getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(gameManager ->
			{
				gameManager.getCurrentGame().ifPresent(game ->
				{
					TriggerContext.Builder builder = TriggerContext.builder().player((ServerPlayer)player).hand(InteractionHand.MAIN_HAND);
					if (entity instanceof LivingEntity living && game.isActive(living))
							builder.target(living);
					game.trigger(TriggerTypes.MELEE.get(), builder);
//					player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager ->
//					{
//						if (manager instanceof ServerPlayerClassManager serverManager)
//						{
//							Player targetPlayer = null;
//							if (entity instanceof Player)
//								targetPlayer = (Player) entity;
//							serverManager.useAbility(game, InteractionHand.MAIN_HAND, AbilityActivation.MELEE, targetPlayer);
//						}
//					});
				});
			});
		}
	}
	
	@SubscribeEvent
	public static void onNameFormat(PlayerEvent.NameFormat event)
	{
		if (event.getDisplayname() instanceof MutableComponent component)
			event.setDisplayname(HuntedNameFormatting.addStyle(event.getEntity(), component));
	}
	
	@SubscribeEvent
	public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event)
	{
		event.setDisplayName(HuntedNameFormatting.addStyle(event.getEntity(), (MutableComponent)event.getEntity().getDisplayName()));
	}
	
	@SubscribeEvent
	public static void onLivingAttack(LivingAttackEvent event)
	{
		if (event.getEntity() instanceof Player && !event.getSource().isBypassInvul())
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event)
	{
		event.getPlayer().getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> event.setCanceled(manager.isInGame() || !event.getPlayer().isCreative()));
	}
	
	@SubscribeEvent
	public static void onItemExpire(ItemExpireEvent event)
	{
		ItemEntity entity = event.getEntity();
		ItemStack stack = entity.getItem();
		if (stack.getOrCreateTag().contains("HuntedGameData"))
		{
			event.setExtraLife(500);
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onTick(TickEvent.LevelTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START)
		{
			if (event.level instanceof ServerLevel level)
			{
				level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
				{
					if (manager.isGameRunning())
					{
						for (Entity entity : level.getAllEntities())
						{
							if (entity instanceof ItemEntity item)
							{
								ItemStack stack = item.getItem();
								if (stack.getOrCreateTag().contains("HuntedGameData"))
								{
									List<ServerPlayer> players = level.players().stream().filter(EntitySelector.NO_SPECTATORS).filter(p -> 
									{
										HuntedClass huntedClass = PlayerClassManager.getClassFor(p);
										if (huntedClass != null && huntedClass.getType().canCollectRewards())
											return true;
										else
											return false;
									}).sorted(Comparator.comparingDouble(item::distanceToSqr)).collect(Collectors.toList());
									ServerPlayer player = null;
									if (players.size() > 0)
										player = players.get(0);
									if (player != null && player.distanceTo(item) > 6.0D)
										item.playerTouch(player);
								}
							}
						}
					}
				});
			}
		}
	}
	
	@SubscribeEvent
	public static void onPickupItem(EntityItemPickupEvent event)
	{
		event.getEntity().getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			manager.getCurrentClass().ifPresent(huntedClass -> 
			{
				if (!huntedClass.getType().canCollectRewards())
				{
					ItemStack stack = event.getItem().getItem();
					if (stack.getOrCreateTag().contains("HuntedGameData"))
						event.setCanceled(true);
				}
			});
		});
	}
}
