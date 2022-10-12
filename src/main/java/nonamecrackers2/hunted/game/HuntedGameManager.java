package nonamecrackers2.hunted.game;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.capability.ServerPlayerClassManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.huntedclass.type.HunterClassType;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.HuntedClassTypes;
import nonamecrackers2.hunted.init.HuntedPacketHandlers;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.packet.UpdateGameInfoPacket;
import nonamecrackers2.hunted.registry.HuntedRegistries;

/**
 * The main class that contains the settings, players, etc. and creates, stops, and begins games
 */
public class HuntedGameManager 
{
	private static final Logger LOGGER = LogManager.getLogger();

	public static final GameType DEFAULT_GAME_MODE = GameType.SURVIVAL;
	private final ServerLevel level;
	private Optional<HuntedGame> currentGame = Optional.empty();
	private final Map<ServerPlayer, HuntedGameManager.HuntedClassHolder> players = Maps.newHashMap();
	private @Nullable HuntedMap map;
	private final RandomSource random;
	private List<Component> overlayText = Lists.newArrayList();
	
	private int totalHunterWins;
	private int totalPreyWins;
	
	public HuntedGameManager(ServerLevel level)
	{
		this.level = level;
		this.random = RandomSource.create();
	}
	
	public void tick()
	{
		if (this.isGameRunning())
		{
			HuntedGame game = this.currentGame.orElse(null);
			game.tick();
			boolean hasUpdated = false;
			var overlay = game.getMap().overlay().orElse(null);
			if (overlay != null)
			{
				List<Component> text = overlay.getText(this.level, game);
				if (!text.equals(this.overlayText))
				{
					this.overlayText = text;
					this.update();
					hasUpdated = true;
				}
			}
			if (!hasUpdated && game.getTimeElapsed() % 240 == 0)
				this.update();
			
			for (ServerPlayer player : this.level.players())
			{
				player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
				{
					if (manager instanceof ServerPlayerClassManager serverManager)
					{
						if (serverManager.requestsUpdate())
							this.update(player);
					}
				});
			}
			
			GameWinContext context = game.getWinStatus();
			if (!context.isEmpty())
				this.finishGame(context);
		}
	}
	
	public void update()
	{
		for (ServerPlayer player : this.level.players())
			this.update(player);
	}
	
	public void update(ServerPlayer player)
	{
		player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (manager instanceof ServerPlayerClassManager serverManager)
			{
				//LOGGER.debug("Updating player: {}, is in game: {}", player, manager.isInGame());
				serverManager.update();
			}
		});
		HuntedMap map = null;
		HuntedGame game = this.currentGame.orElse(null);
		if (game != null)
			map = game.getMap();
		HuntedPacketHandlers.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new UpdateGameInfoPacket(this.isGameRunning(), this.overlayText, map));
	}
	
	public Optional<HuntedGame> getCurrentGame()
	{
		return this.currentGame;
	}
	
	public boolean join(ServerPlayer player, HuntedClass prey, HuntedClass hunter)
	{
		if (prey.isMutable() || hunter.isMutable())
			throw new IllegalArgumentException("Hunted classes must be immutable");
		if (validate(prey, false) && validate(hunter, true))
			return this.players.putIfAbsent(player, new HuntedGameManager.HuntedClassHolder(prey, hunter)) == null ? true : false;
		else
			return false;
	}
	
	public HuntedGameManager.ChangeClassStatus changePreyClass(ServerPlayer player, HuntedClass huntedClass)
	{
		return this.changeClass(player, huntedClass, false);
	}
	
	public HuntedGameManager.ChangeClassStatus changeHunterClass(ServerPlayer player, HuntedClass huntedClass)
	{
		return this.changeClass(player, huntedClass, true);
	}
	
	public HuntedGameManager.ChangeClassStatus changeClass(ServerPlayer player, HuntedClass huntedClass, boolean isHunter)
	{
		if (huntedClass.isMutable())
			throw new IllegalArgumentException("HuntedClass must be immutable");
		if (validate(huntedClass, isHunter))
		{
			if (this.players.containsKey(player))
			{
				HuntedGameManager.HuntedClassHolder holder = this.players.get(player);
				HuntedClass currentClass = isHunter ? holder.hunter : holder.prey;
				if (currentClass.equals(huntedClass))
				{
					return HuntedGameManager.ChangeClassStatus.SAME_CLASS;
				}
				else
				{
					if (isHunter)
						this.players.get(player).hunter = huntedClass;
					else
						this.players.get(player).prey = huntedClass;
					return HuntedGameManager.ChangeClassStatus.SUCCESS;
				}
			}
			else
			{
				return HuntedGameManager.ChangeClassStatus.NOT_IN_GAME;
			}
		}
		else
		{
			return HuntedGameManager.ChangeClassStatus.INVALID_CLASSES;
		}
	}
	
	public boolean leave(ServerPlayer player)
	{
		return this.players.remove(player) != null ? true : false;
	}
	
	public HuntedGameManager.GameStartStatus startGame()
	{
		var iterator = this.players.entrySet().iterator();
		while (iterator.hasNext())
		{
			var entry = iterator.next();
			if (!entry.getKey().isAlive())
				iterator.remove();
		}
		
		if (!this.isGameRunning())
		{
			if (this.players.size() >= 2)
			{
				if (this.map != null)
				{
					List<ServerPlayer> players = this.players.keySet().stream().collect(Collectors.toList());
					
					HuntedGame game = new HuntedGame(this.level, players.stream().collect(Collectors.mapping(ServerPlayer::getUUID, Collectors.toList())), this.map);
					
					int index = this.random.nextInt(players.size());
					ServerPlayer hunter = players.get(index);
					
					this.players.forEach((player, holder) -> 
					{
						player.setGameMode(DEFAULT_GAME_MODE);
						player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> {
							manager.setClass(player.equals(hunter) ? holder.hunter.copy() : holder.prey.copy());
						});
					});
					
					this.currentGame = Optional.of(game);
					game.begin();
					this.update();
					return HuntedGameManager.GameStartStatus.SUCCESS;
				}
				else
				{
					return HuntedGameManager.GameStartStatus.NO_SELECTED_MAP;
				}
			}
			else
			{
				return HuntedGameManager.GameStartStatus.NOT_ENOUGH_PLAYERS;
			}
		}
		else
		{
			return HuntedGameManager.GameStartStatus.GAME_ALREADY_RUNNING;
		}
	}
	
	public void finishGame(GameWinContext context)
	{
		if (this.isGameRunning())
		{
			HuntedGame game = this.currentGame.orElse(null);
			game.finish(context);
			if (context.isPreyWin())
				this.totalPreyWins++;
			else if (context.isHunterWin())
				this.totalHunterWins++;
			this.currentGame = Optional.empty();
			this.overlayText = this.createStatsOverlay();
			this.update();
			this.players.clear();
		}
		else
		{
			LOGGER.warn("There is currently no game running!");
		}
	}
	
	public boolean isGameRunning()
	{
		return this.currentGame.isPresent();
	}
	
	public CompoundTag write()
	{
		CompoundTag tag = new CompoundTag();
		if (this.getCurrentGame().isPresent())
		{
			CompoundTag gameTag = new CompoundTag();
			HuntedGame game = this.getCurrentGame().orElse(null);
			game.save(gameTag);
			tag.put("Game", gameTag);
		}
		tag.putInt("HunterWins", this.totalHunterWins);
		tag.putInt("PreyWins", this.totalPreyWins);
		return tag;
	}
	
	public void read(CompoundTag tag)
	{
		if (tag.contains("Game"))
		{
			try 
			{
				this.currentGame = Optional.of(HuntedGame.read(this.level, tag.getCompound("Game")));
			}
			catch (Exception e)
			{
				LOGGER.error("Failed to read game:", e);
			}
		}
		this.totalHunterWins = tag.getInt("HunterWins");
		this.totalPreyWins = tag.getInt("PreyWins");
		this.overlayText = this.createStatsOverlay();
	}
	
	public void setMap(@Nullable HuntedMap map)
	{
		this.map = map;
	}
	
	private List<Component> createStatsOverlay()
	{
		return Lists.newArrayList(
				createStatText(HuntedClassTypes.HUNTER.get(), this.totalHunterWins),
				createStatText(HuntedClassTypes.PREY.get(), this.totalPreyWins)
		);
	}
	
	public int getTotalPreyWins()
	{
		return this.totalPreyWins;
	}
	
	public int getTotalHunterWins()
	{
		return this.totalHunterWins;
	}
	
	public void resetStats()
	{
		this.totalPreyWins = 0;
		this.totalHunterWins = 0;
		this.overlayText = this.createStatsOverlay();
		this.update();
	}
	
	private static Component createStatText(HuntedClassType type, int wins)
	{
		ResourceLocation id = HuntedRegistries.HUNTED_CLASS_TYPES.get().getKey(type);
		return Component.translatable("hunted.overlay.stats.win", 
				Component.translatable(id.getNamespace() + ".class." + id.getPath()).withStyle(Style.EMPTY.withBold(true).withColor(type.getColor())), 
				Component.literal(String.valueOf(wins)).withStyle(ChatFormatting.RED)
		).withStyle(ChatFormatting.GOLD);
	}
	
	private static boolean validate(HuntedClass huntedClass, boolean mustBeHunter)
	{
		if (!(huntedClass.getType() instanceof HunterClassType) && mustBeHunter)
			return false;
		else if (huntedClass.getType() instanceof HunterClassType && !mustBeHunter)
			return false;
		else
			return true;
	}
	
	public static enum ChangeClassStatus
	{
		INVALID_CLASSES("commands.hunted.game.changeClass.invalid", true),
		NOT_IN_GAME("commands.hunted.game.changeClass.notInGame", true),
		SAME_CLASS("commands.hunted.game.changeClass.sameClass", true),
		SUCCESS("commands.hunted.game.changeClass.success", false);
		
		private final String translation;
		private final boolean isFailure;
		
		private ChangeClassStatus(String translation, boolean isFailure)
		{
			this.translation = translation;
			this.isFailure = isFailure;
		}
		
		public void sendResult(CommandSourceStack stack, Object... objects)
		{
			if (this.isFailure)
				stack.sendFailure(Component.translatable(this.translation, objects));
			else
				stack.sendSuccess(Component.translatable(this.translation, objects), true);
		}
	}
	
	public static enum GameStartStatus
	{
		NO_SELECTED_MAP,
		NOT_ENOUGH_PLAYERS,
		GAME_ALREADY_RUNNING,
		SUCCESS;
	}
	
	public static class HuntedClassHolder
	{
		private HuntedClass prey;
		private HuntedClass hunter;
		
		private HuntedClassHolder(HuntedClass prey, HuntedClass hunter)
		{
			if (prey.getType() instanceof HunterClassType)
				throw new IllegalArgumentException("Prey class must not be the hunter type!");
			if (!(hunter.getType() instanceof HunterClassType))
				throw new IllegalArgumentException("Hunter class must be the hunter type!");
			this.prey = prey;
			this.hunter = hunter;
		}
	}
	
	public static class Events
	{
		@SubscribeEvent
		public static void onWorldTick(TickEvent.LevelTickEvent event)
		{
			if (event.phase == TickEvent.Phase.END)
				event.level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(HuntedGameManager::tick);
		}
	}
}
