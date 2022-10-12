package nonamecrackers2.hunted.resources;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class SimpleDataManager<T> extends SimpleJsonResourceReloadListener
{
	private static final Logger LOGGER = LogManager.getLogger();
	protected Map<ResourceLocation, T> values = ImmutableMap.of();
	protected Map<ResourceLocation, T> syncedValues = ImmutableMap.of();
	protected final String directory;
	protected final Codec<T> codec = RecordCodecBuilder.create(instance -> {
		return instance.group(ResourceLocation.CODEC.fieldOf("type").forGetter(this::getSyncedId)).apply(instance, id -> this.syncedValues().get(id));
	});
	
	public SimpleDataManager(Gson gson, String directory)
	{
		super(gson, directory);
		this.directory = directory;
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller filler)
	{
		ImmutableMap.Builder<ResourceLocation, T> values = ImmutableMap.builder();
		for(Entry<ResourceLocation, JsonElement> entry : map.entrySet())
		{
			ResourceLocation fileName = entry.getKey();
			try
			{
				T t = this.apply(entry, manager, filler);
				if (t != null)
					values.put(fileName, t);
			}
			catch (/*IllegalArgumentException | JsonParseException*/Exception exception)
			{
				LOGGER.error("Parsing error loading {} {}, error: {}", this.directory, fileName, exception);
			}
		}
		this.values = values.build();
		this.syncedValues = ImmutableMap.copyOf(this.values);
		LOGGER.info("Loaded {} {}(s)", this.values.size(), this.directory);
	}
	
	protected abstract @Nullable T apply(Map.Entry<ResourceLocation, JsonElement> file, ResourceManager manager, ProfilerFiller filler);

	public @Nullable T get(ResourceLocation id)
	{
		return this.values.get(id);
	}
	
	public @Nullable T getSynced(ResourceLocation id)
	{
		return this.syncedValues.get(id);
	}
	
	public @Nullable ResourceLocation getId(T value)
	{
		for (Map.Entry<ResourceLocation, T> entry : this.values.entrySet())
		{
			if (entry.getValue() == value)
				return entry.getKey();
		}
		return null;
	}
	
	public @Nullable ResourceLocation getSyncedId(T value)
	{
		for (Map.Entry<ResourceLocation, T> entry : this.syncedValues.entrySet())
		{
			if (entry.getValue() == value)
				return entry.getKey();
		}
		return null;
	}
	
	public Map<ResourceLocation, T> values()
	{
		return ImmutableMap.copyOf(this.values);
	}
	
	public void setSynced(Map<ResourceLocation, T> values)
	{
		this.syncedValues = values;
	}
	
	public Map<ResourceLocation, T> syncedValues()
	{
		return this.syncedValues;
	}
	
	public String getDirectory()
	{
		return this.directory;
	}
	
	public Codec<T> getCodec()
	{
		return this.codec;
	}
}
