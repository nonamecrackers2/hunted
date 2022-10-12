package nonamecrackers2.hunted.game;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.block.entity.KeyholeBlockEntity;
import nonamecrackers2.hunted.capability.HuntedClassManager;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.capability.ServerPlayerClassManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.TriggerTypes;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.map.HuntedMapDataManager;
import nonamecrackers2.hunted.map.event.MapEventHolder;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.rewards.ButtonReward;
import nonamecrackers2.hunted.rewards.ButtonRewardsDataManager;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.DataHolder;
import nonamecrackers2.hunted.util.HuntedUtil;

public class HuntedGame implements DataHolder
{
	private final ServerLevel level;
	private final List<UUID> players;
	private final List<UUID> eliminated = Lists.newArrayList();
	private final List<UUID> escaped = Lists.newArrayList();
	private final HuntedMap map;
	private Map<BlockPos, ButtonReward> availableRewards = Maps.newHashMap();
	private Map<BlockPos, ButtonReward> collectedRewards = Maps.newHashMap();
	//private Map<BlockPos, BlockState> usedButtons = Maps.newHashMap();
	private @Nullable CompoundTag data;
	private int timeElapse;
	private int buttonPressingDelay;

	public HuntedGame(ServerLevel level, List<UUID> players, HuntedMap map)
	{
		this.level = level;
		this.players = players;
		this.map = map;
	}
	
	public void tick()
	{
		this.timeElapse++;
		if (this.timeElapse % 20 == 0)
			this.trigger(TriggerTypes.TIMER.get(), TriggerContext.builder());
		if (this.buttonPressingDelay > 0)
		{
			this.buttonPressingDelay--;
			if (this.buttonPressingDelay == 0)
			{
				for (ServerPlayer player : this.getActive())
				{
					HuntedClass huntedClass = HuntedClassManager.getClassForPlayer(player);
					if (huntedClass != null && huntedClass.getType().canCollectRewards())
						player.sendSystemMessage(Component.translatable("hunted.game.button.active").withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.LIGHT_PURPLE)));
				}
			}
		}
		for (ServerPlayer player : this.getActive())
		{
			player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager ->
			{
				if (manager instanceof ServerPlayerClassManager serverManager)
				{
					serverManager.tick(this.level, this);
					serverManager.getCurrentClass().ifPresent(huntedClass -> 
					{
						if (huntedClass.getType().canEscape() && this.map.preyExit().contains(player.position()))
							this.escape(player);
					});
				}
			});
		}
		List<UUID> escapedOrEliminated = Lists.newArrayList(this.eliminated);
		escapedOrEliminated.addAll(this.escaped);
		for (UUID uuid : escapedOrEliminated)
		{
			ServerPlayer player = (ServerPlayer)this.level.getPlayerByUUID(uuid);
			if (player != null)
			{
				if (!this.map.boundary().contains(player.position()))
					player.moveTo(Vec3.atBottomCenterOf(this.map.defaultStartPos()));
			}
		}
		for (MapEventHolder event : this.map.events())
			event.tick(this.level, this);
//		for (ButtonReward reward : this.getRewards())
//			reward.reset(this.level, this);
	}
	
	public void processButton(ServerPlayer player, BlockPos pos)
	{
		if (this.buttonPressingDelay <= 0 && this.players.contains(player.getUUID()) && this.availableRewards.size() > 0)
		{
			ButtonReward reward = this.availableRewards.get(pos);
			if (reward != null)
			{
				HuntedClass huntedClass = HuntedClassManager.getClassForPlayer(player);
				if (huntedClass != null && huntedClass.getType().canCollectRewards())
				{
					reward.reward(TriggerContext.builder().player(player).reward(reward).build(this.level, TriggerTypes.NONE.get()));
					if (this.availableRewards.remove(pos) != null)
						this.collectedRewards.put(pos, reward);
					//this.usedButtons.put(pos, this.level.getBlockState(pos));
					this.triggerForActive(TriggerTypes.REWARDED.get(), TriggerContext.builder().target(player).reward(reward));
				}
			}
		}
	}
	
	public void begin()
	{
		this.data = null;
		this.buttonPressingDelay = this.map.buttonPressingDelay();
		this.resetMap();
		for (UUID uuid : this.players)
		{
			ServerPlayer player = (ServerPlayer)this.level.getPlayerByUUID(uuid);
			if (player != null)
			{
				player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
				{
					if (manager instanceof ServerPlayerClassManager serverManager)
						serverManager.begin(this.map);
					HuntedClass huntedClass = manager.getCurrentClass().orElse(null);
					if (huntedClass != null)
					{
						HuntedUtil.showTitle(player, Component.translatable(huntedClass.getTranslation()).withStyle(Style.EMPTY.withColor(huntedClass.getType().getColor()).withBold(true)), 20, 60, 20);
						HuntedUtil.showSubtitle(player, Component.translatable(huntedClass.getTranslation() + ".description").withStyle(Style.EMPTY.applyFormat(ChatFormatting.GOLD)), 20, 60, 20);
						for (Ability ability : huntedClass.getAbilities())
							player.getCooldowns().removeCooldown(ability.getItem());
						if (huntedClass.getType().canCollectRewards() && this.buttonPressingDelay > 0)
							player.sendSystemMessage(Component.translatable("hunted.game.button.delay", Component.literal(String.valueOf(this.buttonPressingDelay/20)).withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.LIGHT_PURPLE))).withStyle(ChatFormatting.GOLD));
					}
				});
				player.playNotifySound(SoundEvents.CAT_HISS, SoundSource.PLAYERS, 1.0F, 0.0F);
				player.refreshDisplayName();
				player.refreshTabListName();
				player.setHealth(player.getMaxHealth());
				player.removeAllEffects();
				this.trigger(TriggerTypes.PLAYER_BEGIN.get(), TriggerContext.builder().player(player));
			}
		}
		List<BlockPos> keys = Lists.newArrayList(this.map.buttons());
		List<ButtonReward> values = Lists.newArrayList(this.map.rewards());
		Collections.shuffle(values, new Random());
		this.availableRewards = IntStream.range(0, keys.size()).boxed().collect(Collectors.toMap(keys::get, values::get));
		for (MapEventHolder event : this.map.events())
			event.begin(this.level, this);
//		for (ButtonReward reward : this.getRewards())
//			reward.begin(this.level, this);
		this.trigger(TriggerTypes.GAME_BEGIN.get(), TriggerContext.builder());
	}
	
	public void finish(GameWinContext context)
	{
		if (!context.isEmpty())
		{
			context.winners().forEach((player, huntedClass) -> 
			{
				HuntedUtil.showTitle(player, Component.translatable("hunted.game.finish.win.title").withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.AQUA)), 20, 60, 20);
				HuntedUtil.showSubtitle(player, Component.translatable("hunted.game.finish.win." + HuntedRegistries.HUNTED_CLASS_TYPES.get().getKey(huntedClass.getType()).getPath() + ".subtitle").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)), 20, 60, 20);
				player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 100.0F, 0.0F);
			});
			context.losers().forEach((player, huntedClass) -> 
			{
				HuntedUtil.showTitle(player, Component.translatable("hunted.game.finish.loss.title").withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.RED)), 20, 60, 20);
				HuntedUtil.showSubtitle(player, Component.translatable("hunted.game.finish.loss." + HuntedRegistries.HUNTED_CLASS_TYPES.get().getKey(huntedClass.getType()).getPath() + ".subtitle").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), 20, 60, 20);
				player.playNotifySound(SoundEvents.BAT_DEATH, SoundSource.PLAYERS, 100.0F, 0.0F);
			});
		}
		
		for (ServerPlayer player : this.getPlayers())
		{
			if (player != null)
			{
				player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
				{
					if (manager instanceof ServerPlayerClassManager serverManager)
						serverManager.finish();
				});
				player.refreshDisplayName();
				player.refreshTabListName();
				player.moveTo(Vec3.atBottomCenterOf(player.getLevel().getSharedSpawnPos()));
				player.removeAllEffects();
				player.setGameMode(GameType.SURVIVAL);
			}
		}
		
		for (MapEventHolder event : this.map.events())
			event.reset(this.level, this);
//		for (ButtonReward reward : this.getRewards())
//			reward.reset(this.level, this);
		
		this.resetMap();
		
		this.data = new CompoundTag();
	}
	
	/*
	 * Returns all players that are participating in the game
	 */
	public List<ServerPlayer> getPlayers()
	{
		List<UUID> uuids = Lists.newArrayList(this.players);
		uuids.addAll(this.eliminated);
		uuids.addAll(this.escaped);
		List<ServerPlayer> players = Lists.newArrayList();
		for (UUID uuid : uuids)
		{
			ServerPlayer player = (ServerPlayer)this.level.getPlayerByUUID(uuid);
			if (player != null)
				players.add(player);
		}
		return players;
	}
	
	public List<ServerPlayer> getActive()
	{
		List<ServerPlayer> players = Lists.newArrayList();
		for (UUID uuid : this.players)
		{
			ServerPlayer player = (ServerPlayer)this.level.getPlayerByUUID(uuid);
			if (player != null)
				players.add(player);
		}
		return players;
	}
	
	public List<ServerPlayer> getEliminated()
	{
		List<ServerPlayer> players = Lists.newArrayList();
		for (UUID uuid : this.eliminated)
		{
			ServerPlayer player = (ServerPlayer)this.level.getPlayerByUUID(uuid);
			if (player != null)
				players.add(player);
		}
		return players;
	}
	
	public List<ServerPlayer> getEscaped()
	{
		List<ServerPlayer> players = Lists.newArrayList();
		for (UUID uuid : this.escaped)
		{
			ServerPlayer player = (ServerPlayer)this.level.getPlayerByUUID(uuid);
			if (player != null)
				players.add(player);
		}
		return players;
	}
	
	public boolean isPlayerEliminated(ServerPlayer player)
	{
		return this.eliminated.contains(player.getUUID());
	}
	
	public boolean hasPlayerEscaped(ServerPlayer player)
	{
		return this.escaped.contains(player.getUUID());
	}
	
	public void eliminate(ServerPlayer player)
	{
		player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (manager instanceof ServerPlayerClassManager serverManager)
			{
				HuntedClass huntedClass = manager.getCurrentClass().orElse(null);
				if (huntedClass != null && this.players.contains(player.getUUID()))
				{
					huntedClass.runDeathSequence(player, this, serverManager.getOrCreateTag());
					serverManager.setUpdateRequest(true);
				}
			}
		});
	}
	
	public void removePlayer(ServerPlayer player)
	{
		UUID uuid = player.getUUID();
		if (this.players.contains(uuid))
		{
			this.players.remove(uuid);
			this.eliminated.add(uuid);
			player.setGameMode(GameType.SPECTATOR);
			this.triggerForActive(TriggerTypes.ELIMINATED.get(), TriggerContext.builder().target(player));
		}
	}
	
	public void escape(ServerPlayer player)
	{
		UUID uuid = player.getUUID();
		if (this.players.contains(uuid))
		{
			this.players.remove(uuid);
			this.escaped.add(uuid);
			player.setGameMode(GameType.SPECTATOR);
			player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
			{
				if (manager instanceof ServerPlayerClassManager serverManager)
					serverManager.setUpdateRequest(true);
			});
			player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 300.0F, 0.0F);
		}
	}
	
	public GameWinContext getWinStatus()
	{
		HuntedClassType winning = null;
		
		List<ServerPlayer> players = this.getPlayers();
		
		for (ServerPlayer player : players)
		{
			HuntedClass huntedClass = HuntedClassManager.getClassForPlayer(player);
			if (huntedClass != null)
			{
				if (huntedClass.getType().checkObjective(this.level, this, player, huntedClass))
				{
					winning = huntedClass.getType();
					break;
				}
			}
		}
		
		Map<ServerPlayer, HuntedClass> winningPlayers = Maps.newHashMap();
		Map<ServerPlayer, HuntedClass> losingPlayers = Maps.newHashMap();
		if (winning != null)
		{
			for (ServerPlayer player : players)
			{
				PlayerClassManager manager = player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
				if (manager != null)
				{
					HuntedClass huntedClass = manager.getCurrentClass().orElse(null);
					if (huntedClass != null)
					{
						if (huntedClass.getType() == winning && manager.isInGame() || huntedClass.getType().checkPartialObjective(this.level, this, player, huntedClass))
							winningPlayers.put(player, huntedClass);
						else
							losingPlayers.put(player, huntedClass);
					}
				}
			}
			
			return new GameWinContext(winning, winningPlayers, losingPlayers);
		}
		else
		{
			return GameWinContext.empty();
		}
	}
	
	public HuntedMap getMap()
	{
		return this.map;
	}
	
	@Override
	public CompoundTag getTag()
	{
		return this.data;
	}
	
	@Override
	public void setTag(CompoundTag tag)
	{
		this.data = tag;
	}
	
	/**
	 * Pass in a trigger along with the required context to activate abilities/events
	 * that use that trigger.
	 * <br><br>
	 * Triggers that activate abilities must always support the activation player
	 * parameter. Abilities do not support triggers that do not support the activation
	 * player.
	 * 
	 * @param trigger the trigger to use
	 * @param builder the unbuilt trigger context
	 * @throws IllegalArgumentException if trigger context does not match trigger requirements
	 */
	public void trigger(Trigger<?> trigger, TriggerContext.Builder builder)
	{
		TriggerContext context = builder.build(this.level, trigger);
		if (context.player() != null)
		{
			context.player().getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
			{
				if (manager instanceof ServerPlayerClassManager serverManager)
				{
					if (serverManager.triggerCriteria().matches(context))
						serverManager.use(context);
				}
			});
		}
		for (MapEventHolder event : this.map.events())
			event.use(context);
	}
	
	public void triggerForActive(Trigger<?> trigger, TriggerContext.Builder builder)
	{
		for (ServerPlayer player : this.getPlayers())
			this.trigger(trigger, builder.player(player));
//		for (ServerPlayer player : this.getPlayers())
//		{
//			TriggerContext context = builder.player(player).build(this.level, trigger);
//			player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
//			{
//				if (manager instanceof ServerPlayerClassManager serverManager)
//				{
//					if (serverManager.triggerCriteria().matches(context))
//						serverManager.use(context);
//				}
//			});
//		}
//		TriggerContext context = builder.build(this.level, trigger);
//		for (MapEventHolder event : this.map.events())
//			event.use(context);
	}
	
	public void save(CompoundTag tag)
	{
		tag.put("Players", saveUUIDList(this.players));
		tag.put("Eliminated", saveUUIDList(this.eliminated));
		tag.put("Escaped", saveUUIDList(this.escaped));
		tag.putString("MapID", this.map.id().toString());
		tag.put("Rewards", saveRewardMap(this.availableRewards));
		tag.put("CollectedRewards", saveRewardMap(this.collectedRewards));
		if (this.data != null)
			tag.put("Data", this.data);
		tag.putInt("TimeElapsed", this.timeElapse);
//		ListTag buttons = new ListTag();
//		for (var entry : this.usedButtons.entrySet())
//		{
//			CompoundTag button = new CompoundTag();
//			button.put("Pos", NbtUtils.writeBlockPos(entry.getKey()));
//			button.put("State", NbtUtils.writeBlockState(entry.getValue()));
//		}
//		tag.put("ButtonsUsed", buttons);
	}
	
	private void resetMap()
	{
		for (BlockPos pos : this.map.keyholes())
		{
			BlockEntity entity = this.level.getBlockEntity(pos);
			if (entity instanceof KeyholeBlockEntity keyholeEntity)
			{
				keyholeEntity.setItem(ItemStack.EMPTY);
				keyholeEntity.setChanged();
			}
		}
		for (Entity entity : this.level.getAllEntities())
		{
			if (entity instanceof ItemEntity item)
			{
				ItemStack stack = item.getItem();
				if (stack.getOrCreateTag().contains("HuntedGameData"))
					item.discard();
			}
		}
//		this.usedButtons.forEach((pos, state) -> this.level.setBlock(pos, state, 3));
	}
	
	public int getTimeElapsed()
	{
		return this.timeElapse;
	}
	
	public List<ButtonReward> getCollectedRewards()
	{
		return ImmutableList.copyOf(this.collectedRewards.values());
	}
	
	public List<ServerPlayer> getActiveBy(Class<? extends HuntedClassType> type)
	{
		return sortBy(this.getActive(), type);
	}
	
	public List<ServerPlayer> getPlayersBy(Class<? extends HuntedClassType> type)
	{
		return sortBy(this.getPlayers(), type);
	}
	
	public List<ButtonReward> getRewards()
	{
		List<ButtonReward> list = Lists.newArrayList(this.availableRewards.values());
		list.addAll(this.collectedRewards.values());
		return list;
	}
	
	public static HuntedGame read(ServerLevel level, CompoundTag tag) throws NullPointerException
	{
		List<UUID> players = readUUIDList(tag.getList("Players", 11));
		List<UUID> eliminated = readUUIDList(tag.getList("Eliminated", 11));
		List<UUID> escaped = readUUIDList(tag.getList("Escaped", 11));
		ResourceLocation mapId = new ResourceLocation(tag.getString("MapID"));
		HuntedMap map = HuntedMapDataManager.INSTANCE.get(mapId);
		if (map == null)
			throw new NullPointerException("Failed to load map");
		HuntedGame game = new HuntedGame(level, players, map);
		game.availableRewards = readRewardMap(tag.getList("Rewards", 10));
		game.collectedRewards = readRewardMap(tag.getList("CollectedRewards", 10));
		for (UUID uuid : eliminated)
			game.eliminated.add(uuid);
		for (UUID uuid : escaped)
			game.escaped.add(uuid);
		if (tag.contains("Data"))
			game.data = tag.getCompound("Data");
		game.timeElapse = tag.getInt("TimeElapsed");
//		Map<BlockPos, BlockState> usedButtons = Maps.newHashMap();
//		ListTag buttons = tag.getList("ButtonsUsed", 10);
//		for (int i = 0; i < buttons.size(); i++)
//		{
//			CompoundTag button = buttons.getCompound(i);
//			usedButtons.put(NbtUtils.readBlockPos(button.getCompound("Pos")), NbtUtils.readBlockState(button.getCompound("State")));
//		}
//		game.usedButtons = usedButtons;
		return game;
	}
	
	private static ListTag saveUUIDList(List<UUID> list)
	{
		ListTag listTag = new ListTag();
		list.forEach(uuid -> listTag.add(NbtUtils.createUUID(uuid)));
		return listTag;
	}
	
	private static ListTag saveRewardMap(Map<BlockPos, ButtonReward> rewards)
	{
		ListTag list = new ListTag();
		rewards.forEach((pos, reward) -> 
		{
			CompoundTag rewardTag = new CompoundTag();
			rewardTag.put("Pos", NbtUtils.writeBlockPos(pos));
			rewardTag.putString("Reward", reward.getId().toString());
			list.add(rewardTag);
		});
		return list;
	}
	
	private static List<UUID> readUUIDList(ListTag tag)
	{
		List<UUID> list = Lists.newArrayList();
		tag.forEach(uuid -> list.add(NbtUtils.loadUUID(uuid)));
		return list;
	}
	
	private static Map<BlockPos, ButtonReward> readRewardMap(ListTag tag)
	{
		Map<BlockPos, ButtonReward> rewards = Maps.newHashMap();
		for (int i = 0; i < tag.size(); i++)
		{
			CompoundTag rewardTag = tag.getCompound(i);
			BlockPos pos = NbtUtils.readBlockPos(rewardTag.getCompound("Pos"));
			ButtonReward reward = ButtonRewardsDataManager.INSTANCE.get(new ResourceLocation(rewardTag.getString("Reward")));
			rewards.putIfAbsent(pos, reward);
		}
		return rewards;
	}
	
	public static List<ServerPlayer> sortBy(List<ServerPlayer> player, Class<? extends HuntedClassType> type)
	{
		return player.stream().filter(p -> 
		{
			HuntedClass huntedClass = HuntedClassManager.getClassForPlayer(p);
			return huntedClass != null && huntedClass.getType().getClass().isAssignableFrom(type);
		}).collect(Collectors.toList());
	}
}
