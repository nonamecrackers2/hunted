package nonamecrackers2.hunted.commands.argument;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.HuntedClassDataManager;
import nonamecrackers2.hunted.huntedclass.type.HunterClassType;

public class HuntedClassArgument implements ArgumentType<ResourceLocation>
{
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_CLASS = new DynamicCommandExceptionType((object) -> {
		return Component.translatable("hunted.class.notFound", object);
	});
	public static final DynamicCommandExceptionType ERROR_NOT_HUNTER_CLASS = new DynamicCommandExceptionType((object) -> {
		return Component.translatable("hunted.class.notHunter", object);
	});
	public static final DynamicCommandExceptionType ERROR_IS_HUNTER_CLASS = new DynamicCommandExceptionType((object) -> {
		return Component.translatable("hunted.class.isHunter", object);
	});
	
	private final boolean hunterClass;
	
	private HuntedClassArgument(boolean hunterClass)
	{
		this.hunterClass = hunterClass;
	}
	
	public static HuntedClassArgument normal()
	{
		return new HuntedClassArgument(false);
	}
	
	public static HuntedClassArgument hunter()
	{
		return new HuntedClassArgument(true);
	}
	
	public static ResourceLocation getNormalClassId(CommandContext<CommandSourceStack> context, String arg) throws CommandSyntaxException
	{
		return verifyClass(context.getArgument(arg, ResourceLocation.class), false);
	}
	
	public static ResourceLocation getHunterClassId(CommandContext<CommandSourceStack> context, String arg) throws CommandSyntaxException
	{
		return verifyClass(context.getArgument(arg, ResourceLocation.class), true);
	}
	
	private static ResourceLocation verifyClass(ResourceLocation id, boolean hunterClass) throws CommandSyntaxException
	{
		HuntedClassDataManager manager = HuntedClassDataManager.INSTANCE;
		HuntedClass huntedClass = manager.syncedValues().get(id);
		if (huntedClass == null)
			throw ERROR_UNKNOWN_CLASS.create(id);
		if (huntedClass.getType() instanceof HunterClassType && !hunterClass)
			throw ERROR_IS_HUNTER_CLASS.create(id);
		if (!(huntedClass.getType() instanceof HunterClassType) && hunterClass)
			throw ERROR_NOT_HUNTER_CLASS.create(id);
		return id;
	}
	
	@Override
	public ResourceLocation parse(StringReader reader) throws CommandSyntaxException 
	{
		return verifyClass(ResourceLocation.read(reader), this.hunterClass);
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) 
	{
		return SharedSuggestionProvider.suggest(HuntedClassDataManager.INSTANCE.syncedValues().values().stream().filter(huntedClass -> {
			if (this.hunterClass)
				return huntedClass.getType() instanceof HunterClassType;
			else
				return !(huntedClass.getType() instanceof HunterClassType);
		}).collect(Collectors.mapping(h -> h.id().toString(), Collectors.toList())), builder);
	}
	
	public static class Info implements ArgumentTypeInfo<HuntedClassArgument, HuntedClassArgument.Info.Template>
	{
		@Override
		public void serializeToNetwork(Template template, FriendlyByteBuf buffer)
		{
			buffer.writeBoolean(template.hunterClass);
		}

		@Override
		public Template deserializeFromNetwork(FriendlyByteBuf buffer)
		{
			return new Template(buffer.readBoolean());
		}

		@Override
		public void serializeToJson(Template template, JsonObject object)
		{
			object.addProperty("hunterClass", template.hunterClass ? "hunter" : "normal");
		}

		@Override
		public Template unpack(HuntedClassArgument argument)
		{
			return new Template(argument.hunterClass);
		}
		
		public final class Template implements ArgumentTypeInfo.Template<HuntedClassArgument>
		{
			private final boolean hunterClass;
			
			private Template(boolean hunterClass)
			{
				this.hunterClass = hunterClass;
			}

			@Override
			public HuntedClassArgument instantiate(CommandBuildContext p_235378_)
			{
				return new HuntedClassArgument(this.hunterClass);
			}

			@Override
			public ArgumentTypeInfo<HuntedClassArgument, ?> type()
			{
				return Info.this;
			}
		}
	}
}
