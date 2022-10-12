package nonamecrackers2.hunted.death;

import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.resources.SimpleDataManager;

public class DeathSequenceDataManager extends SimpleDataManager<DeathSequence.ConfiguredDeathSequence<?>>
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final DeathSequenceDataManager INSTANCE = new DeathSequenceDataManager();
	
	public DeathSequenceDataManager()
	{
		super(GSON, "death_sequences");
	}

	@Override
	protected DeathSequence.ConfiguredDeathSequence<?> apply(Entry<ResourceLocation, JsonElement> file, ResourceManager manager, ProfilerFiller filler)
	{
		JsonObject object = GsonHelper.convertToJsonObject(file.getValue(), "death_sequence");
		String rawType = GsonHelper.getAsString(object, "type");
		DeathSequence<?> sequence = HuntedRegistries.DEATH_SEQUENCES.get().getValue(new ResourceLocation(rawType));
		if (sequence != null)
			return sequence.configure(object.get("settings"));
		else
			throw new JsonSyntaxException("Invalid or unsupported Death Sequence '" + rawType + "'");
	}
}
