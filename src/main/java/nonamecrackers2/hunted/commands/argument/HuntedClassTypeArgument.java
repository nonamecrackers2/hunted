package nonamecrackers2.hunted.commands.argument;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.registry.HuntedRegistries;

public class HuntedClassTypeArgument implements ArgumentType<HuntedClassType>
{
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_TYPE = new DynamicCommandExceptionType((object) -> {
		return Component.translatable("hunted.class.type.notFound", object);
	});
	
	private HuntedClassTypeArgument() {}
	
	public static HuntedClassTypeArgument type()
	{
		return new HuntedClassTypeArgument();
	}
	
	public static HuntedClassType getType(CommandContext<CommandSourceStack> context, String id)
	{
		return context.getArgument(id, HuntedClassType.class);
	}
	
	@Override
	public HuntedClassType parse(StringReader reader) throws CommandSyntaxException 
	{
		ResourceLocation id = ResourceLocation.read(reader);
		return Optional.ofNullable(HuntedRegistries.HUNTED_CLASS_TYPES.get().getValue(id)).orElseThrow(() -> {
			return ERROR_UNKNOWN_TYPE.create(id);
		});
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) 
	{
		return SharedSuggestionProvider.suggestResource(HuntedRegistries.HUNTED_CLASS_TYPES.get().getKeys(), builder);
	}
}
