package nonamecrackers2.hunted.commands.argument;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.map.HuntedMapDataManager;

public class HuntedMapArgument implements ArgumentType<ResourceLocation>
{
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_MAP = new DynamicCommandExceptionType((object) -> {
		return Component.translatable("hunted.map.notFound", object);
	});
	
	public static HuntedMapArgument id()
	{
		return new HuntedMapArgument();
	}
	
	public static ResourceLocation getMapId(CommandContext<CommandSourceStack> context, String arg) throws CommandSyntaxException
	{
		return verifyMap(context.getArgument(arg, ResourceLocation.class));
	}
	
	private static ResourceLocation verifyMap(ResourceLocation id) throws CommandSyntaxException
	{
		HuntedMapDataManager manager = HuntedMapDataManager.INSTANCE;
		HuntedMap map = manager.syncedValues().get(id);
		if (map == null)
			throw ERROR_UNKNOWN_MAP.create(id);
		return id;
	}
	
	@Override
	public ResourceLocation parse(StringReader reader) throws CommandSyntaxException 
	{
		return verifyMap(ResourceLocation.read(reader));
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) 
	{
		return SharedSuggestionProvider.suggest(HuntedMapDataManager.INSTANCE.syncedValues().keySet().stream().collect(Collectors.mapping(ResourceLocation::toString, Collectors.toList())), builder);
	}
}
