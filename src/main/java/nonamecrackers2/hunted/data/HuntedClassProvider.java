package nonamecrackers2.hunted.data;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

public class HuntedClassProvider implements DataProvider
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
	
	private final DataGenerator generator;
	
	public HuntedClassProvider(DataGenerator generator)
	{
		this.generator = generator;
	}

	@Override
	public String getName() 
	{
		return null;
	}

	@Override
	public void run(CachedOutput p_236071_) throws IOException 
	{
	}
}
