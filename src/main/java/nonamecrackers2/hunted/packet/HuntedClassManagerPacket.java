package nonamecrackers2.hunted.packet;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ibm.icu.impl.locale.XCldrStub.ImmutableMap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.ability.AbilityDataManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.HuntedClassDataManager;
import nonamecrackers2.hunted.resources.SimpleDataManager;

public class HuntedClassManagerPacket extends SimpleDataManagerPacket<HuntedClass>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Map<ResourceLocation, List<ResourceLocation>> abilities = Maps.newHashMap();
	
	public HuntedClassManagerPacket(Map<ResourceLocation, HuntedClass> values)
	{
		super(values);
	}
	
	public HuntedClassManagerPacket()
	{
		super();
	}
	
	@Override
	protected BiConsumer<HuntedClass, FriendlyByteBuf> encode()
	{
		return (huntedClass, buffer) -> 
		{
			huntedClass.toPacket(buffer);
			buffer.writeVarInt(huntedClass.getAbilities().size());
			huntedClass.getAbilities().forEach(ability -> buffer.writeResourceLocation(ability.id()));
		};
	}
	
	@Override
	protected Function<FriendlyByteBuf, HuntedClass> decode()
	{
		return buffer -> 
		{
			HuntedClass huntedClass = HuntedClass.fromPacket(buffer);
			int abilitySize = buffer.readVarInt();
			List<ResourceLocation> abilities = Lists.newArrayList();
			for (int i = 0; i < abilitySize; i++)
				abilities.add(buffer.readResourceLocation());
			this.abilities.computeIfAbsent(huntedClass.id(), (id) -> abilities);
			return huntedClass;
		};
	}
	
	@Override
	protected SimpleDataManager<HuntedClass> manager()
	{
		return HuntedClassDataManager.INSTANCE;
	}
	
	@Override
	public Runnable getProcessor(NetworkEvent.Context context)
	{
		return () -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
		{
			LOGGER.debug("Received {} {}(s)", this.values.size(), this.manager().getDirectory());
			for (var entry : this.abilities.entrySet())
			{
				List<Ability> abilities = Lists.newArrayList();
				for (ResourceLocation abilityId : entry.getValue())
				{
					Ability ability = AbilityDataManager.INSTANCE.getSynced(abilityId);
					if (ability != null)
						abilities.add(ability);
					else
						LOGGER.error("Received unknown ability '{}'!", abilityId);
				}
				this.values.get(entry.getKey()).setAbilities(ImmutableList.copyOf(abilities));
			}
			this.manager().setSynced(ImmutableMap.copyOf(this.values));
		});
	}
}
