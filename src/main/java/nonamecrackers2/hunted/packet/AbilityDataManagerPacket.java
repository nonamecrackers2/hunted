package nonamecrackers2.hunted.packet;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.ability.AbilityDataManager;
import nonamecrackers2.hunted.resources.SimpleDataManager;

public class AbilityDataManagerPacket extends SimpleDataManagerPacket<Ability>
{
	public AbilityDataManagerPacket(Map<ResourceLocation, Ability> values)
	{
		super(values);
	}
	
	public AbilityDataManagerPacket()
	{
		super();
	}
	
	@Override
	protected BiConsumer<Ability, FriendlyByteBuf> encode()
	{
		return Ability::toPacket;
	}

	@Override
	protected Function<FriendlyByteBuf, Ability> decode()
	{
		return Ability::fromPacket;
	}

	@Override
	protected SimpleDataManager<Ability> manager()
	{
		return AbilityDataManager.INSTANCE;
	}
}
