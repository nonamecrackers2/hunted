package nonamecrackers2.hunted.commands;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import nonamecrackers2.hunted.commands.argument.HuntedClassArgument;
import nonamecrackers2.hunted.commands.argument.HuntedClassTypeArgument;
import nonamecrackers2.hunted.commands.argument.HuntedMapArgument;
import nonamecrackers2.hunted.game.GameWinContext;
import nonamecrackers2.hunted.game.HuntedGameManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.HuntedClassDataManager;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.HuntedClassTypes;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.map.HuntedMapDataManager;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.util.HuntedClassSelector;

public class HuntedCommands 
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> setPhaseCommand =
				Commands.literal("hunted")
					.requires((commandSource) -> commandSource.hasPermission(2))
					.then(Commands.literal("game")
							.then(Commands.literal("join")
									.then(Commands.argument("player", EntityArgument.player())
											.then(Commands.argument("prey", HuntedClassArgument.normal())
													.then(Commands.argument("hunter", HuntedClassArgument.hunter())
													.executes(HuntedCommands::joinGame))
											)
									)
							)
//							.then(Commands.literal("class")
//									.then(Commands.literal("set")
//											.then(Commands.literal("prey")
//													.then(Commands.argument("player", EntityArgument.player())
//															.then(Commands.argument("class", HuntedClassArgument.normal())
//															.executes(context -> setClass(context, false)))
//													)
//											)
//											.then(Commands.literal("hunter")
//													.then(Commands.argument("player", EntityArgument.player())
//															.then(Commands.argument("class", HuntedClassArgument.hunter())
//															.executes(context -> setClass(context, true)))
//													)
//											)
//									)
//							)
							.then(Commands.literal("leave")
									.then(
											Commands.argument("player", EntityArgument.player())
											.executes(HuntedCommands::leaveGame)
									)
							)
							.then(Commands.literal("begin")
									.then(Commands.argument("map", HuntedMapArgument.id())
									.executes(HuntedCommands::startGame))
							)
							.then(Commands.literal("stop")
									.executes(HuntedCommands::stopGame)
							)
					)
					.then(Commands.literal("class")
							.then(Commands.literal("list")
									.then(Commands.argument("type", HuntedClassTypeArgument.type())
											.executes(HuntedCommands::listClasses)
									)
							)
					)
					.then(Commands.literal("stats")
							.then(Commands.literal("reset")
									.executes(HuntedCommands::resetStats)
							)
					);
		dispatcher.register(setPhaseCommand);
	}
	
	public static int joinGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		CommandSourceStack source = context.getSource();
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		ResourceLocation hunterId = HuntedClassArgument.getHunterClassId(context, "hunter");
		ResourceLocation normalId = HuntedClassArgument.getNormalClassId(context, "prey");
		HuntedClass hunterClass = HuntedClassDataManager.INSTANCE.get(hunterId);
		HuntedClass normalClass = HuntedClassDataManager.INSTANCE.get(normalId);
		ServerLevel level = source.getLevel();
		level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
		{
			if (manager.join(player, HuntedClassSelector.builder().setSelected(HuntedClassTypes.HUNTER.get(), hunterClass).setSelected(HuntedClassTypes.PREY.get(), normalClass).build()))
				source.sendSuccess(Component.translatable("commands.hunted.game.join.success", player.getDisplayName()), true);
			else
				source.sendFailure(Component.translatable("commands.hunted.game.join.fail"));
		});
		return 0;
	}
	
//	public static int setClass(CommandContext<CommandSourceStack> context, boolean isHunter) throws CommandSyntaxException
//	{
//		CommandSourceStack source = context.getSource();
//		ServerPlayer player = EntityArgument.getPlayer(context, "player");
//		ResourceLocation id = null;
//		if (isHunter)
//			id = HuntedClassArgument.getHunterClassId(context, "class");
//		else
//			id = HuntedClassArgument.getNormalClassId(context, "class");
//		HuntedClass huntedClass = HuntedClassDataManager.INSTANCE.get(id);
//		ServerLevel level = source.getLevel();
//		level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> {
//			manager.changeClass(player, huntedClass, isHunter).sendResult(source, player.getDisplayName());
//		});
//		return 0;
//	}
	
	public static int leaveGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		CommandSourceStack source = context.getSource();
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		ServerLevel level = source.getLevel();
		level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
		{
			if (manager.leave(player))
				source.sendSuccess(Component.translatable("commands.hunted.game.leave.success", player.getDisplayName()), true);
			else
				source.sendFailure(Component.translatable("commands.hunted.game.leave.fail"));
		});
		return 0;
	}
	
	public static int startGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		CommandSourceStack source = context.getSource();
		ServerLevel level = source.getLevel();
		ResourceLocation id = HuntedMapArgument.getMapId(context, "map");
		HuntedMap map = HuntedMapDataManager.INSTANCE.get(id);
		level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
		{
			manager.setMap(map);
			HuntedGameManager.GameStartStatus status = manager.startGame();
			switch (status)
			{
				case GAME_ALREADY_RUNNING:
				{
					source.sendFailure(Component.translatable("commands.hunted.game.start.alreadyRunning"));
					break;
				}
				case NOT_ENOUGH_PLAYERS:
				{
					source.sendFailure(Component.translatable("commands.hunted.game.start.notEnoughPlayers"));
					break;
				}
				case SUCCESS:
				{
					break;
				}
				default:
					source.sendFailure(Component.translatable("commands.hunted.game.start.error"));
			}
		});
		return 0;
	}
	
	public static int stopGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		CommandSourceStack source = context.getSource();
		ServerLevel level = source.getLevel();
		level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
		{
			if (manager.isGameRunning())
				manager.finishGame(GameWinContext.empty());
			else
				source.sendFailure(Component.translatable("commands.hunted.game.finish.fail"));
		});
		return 0;
	}
	
	public static int listClasses(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		CommandSourceStack source = context.getSource();
		HuntedClassType type = HuntedClassTypeArgument.getType(context, "type");
		Map<ResourceLocation, HuntedClass> classes = HuntedClassDataManager.INSTANCE.values();
		source.sendSuccess(Component.translatable("commands.hunted.class.list", HuntedRegistries.HUNTED_CLASS_TYPES.get().getKey(type)).withStyle(ChatFormatting.GOLD), false);
		int i = 0;
		for (var entry : classes.entrySet())
		{
			if (entry.getValue().getType().equals(type))
			{
				i++;
				source.sendSuccess(Component.literal(i + ". ").withStyle(ChatFormatting.LIGHT_PURPLE).append(Component.literal("'" + entry.getKey() + "'").withStyle(ChatFormatting.GRAY)), false);
			}
		}
		if (i == 0)
			source.sendSuccess(Component.literal("0. ").withStyle(ChatFormatting.RED), false);
		return classes.size();
	}
	
	public static int resetStats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		CommandSourceStack source = context.getSource();
		ServerLevel level = source.getLevel();
		level.getCapability(HuntedCapabilities.GAME_MANAGER).ifPresent(manager -> 
		{
			if (manager.getTotalHunterWins() > 0 || manager.getTotalPreyWins() > 0)
			{
				manager.resetStats();
				source.sendSuccess(Component.translatable("commands.hunted.stats.reset"), true);
			}
			else
			{
				source.sendFailure(Component.translatable("commands.hunted.stats.reset.fail"));
			}
		});
		return 0;
	}
}
