package nonamecrackers2.hunted.packet;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
	private Map<ResourceLocation, List<ResourceLocation>> passiveAbilities = Maps.newHashMap();
	
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
			buffer.writeCollection(huntedClass.getAbilities(), (b, a) -> b.writeResourceLocation(a.id()));
			buffer.writeCollection(huntedClass.getPassiveAbilities(), (b, a) -> b.writeResourceLocation(a.id()));
//			buffer.writeVarInt(huntedClass.getAllAbilities().size());
//			huntedClass.getAllAbilities().forEach(ability -> buffer.writeResourceLocation(ability.id()));
		};
	}
	
	@Override
	protected Function<FriendlyByteBuf, HuntedClass> decode()
	{
		return buffer -> 
		{
			HuntedClass huntedClass = HuntedClass.fromPacket(buffer);
			List<ResourceLocation> abilities = buffer.readList(FriendlyByteBuf::readResourceLocation);
			this.abilities.computeIfAbsent(huntedClass.id(), (id) -> abilities);
			List<ResourceLocation> passiveAbilities = buffer.readList(FriendlyByteBuf::readResourceLocation);
			this.passiveAbilities.computeIfAbsent(huntedClass.id(), (id) -> passiveAbilities);
//			int abilitySize = buffer.readVarInt();
//			List<ResourceLocation> abilities = Lists.newArrayList();
//			for (int i = 0; i < abilitySize; i++)
//				abilities.add(buffer.readResourceLocation());
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
				List<Ability> abilities = getAbilities(entry.getValue());
				this.values.get(entry.getKey()).setAbilities(ImmutableList.copyOf(abilities));
			}
			for (var entry : this.passiveAbilities.entrySet())
			{
				List<Ability> abilities = getAbilities(entry.getValue());
				this.values.get(entry.getKey()).setPassiveAbilities(ImmutableList.copyOf(abilities));
			}
			this.manager().setSynced(ImmutableMap.copyOf(this.values));
		});
	}
	
	private static List<Ability> getAbilities(List<ResourceLocation> abilityLocs)
	{
		List<Ability> abilities = Lists.newArrayList();
		for (ResourceLocation abilityId : abilityLocs)
		{
			Ability ability = AbilityDataManager.INSTANCE.getSynced(abilityId);
			if (ability != null)
				abilities.add(ability);
			else
				LOGGER.error("Received unknown ability '{}'!", abilityId);
		}
		return abilities;
	}
}
