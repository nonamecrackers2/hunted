package nonamecrackers2.hunted.util;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
import com.ibm.icu.impl.locale.XCldrStub.ImmutableMap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.HuntedClassDataManager;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.registry.HuntedRegistries;

public class HuntedClassSelector
{
	private static final Logger LOGGER = LogManager.getLogger();
	private final Map<HuntedClassType, HuntedClass> classesByType;
	
	private HuntedClassSelector(Map<HuntedClassType, HuntedClass> selected)
	{
		this.classesByType = selected;
	}
	
	public boolean hasType(HuntedClassType type)
	{
		return this.classesByType.containsKey(type);
	}
	
	public void verify(HuntedClassType... types)
	{
		for (var type : types)
		{
			if (!this.hasType(type))
				throw new NullPointerException("Selected class(es) do not contain type '" + type + "'");
		}
	}
	
	public @Nullable HuntedClass getFromType(HuntedClassType type)
	{
		if (!this.classesByType.containsKey(type))
			throw new NullPointerException("Does not contain type '" + type + "'");
		return this.classesByType.get(type);
	}
	
	public static HuntedClassSelector.Builder builder()
	{
		return new HuntedClassSelector.Builder();
	}
	
	public void toPacket(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(this.classesByType.size());
		for (var entry : this.classesByType.entrySet())
		{
			buffer.writeRegistryId(HuntedRegistries.HUNTED_CLASS_TYPES.get(), entry.getKey());
			buffer.writeResourceLocation(entry.getValue().id());
		}
	}
	
	public static HuntedClassSelector fromPacket(FriendlyByteBuf buffer)
	{
		HuntedClassSelector.Builder builder = builder();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
		{
			HuntedClassType type = buffer.readRegistryId();
			ResourceLocation id = buffer.readResourceLocation();
			HuntedClass huntedClass = HuntedClassDataManager.INSTANCE.getSynced(id);
			if (huntedClass != null)
				builder.setSelected(type, huntedClass);
			else
				LOGGER.warn("Received unknown class '" + id + "'");
		}
		return builder.build();
	}
	
	public HuntedClassSelector.Builder toBuilder()
	{
		var builder = builder();
		builder.classesByType.putAll(this.classesByType);
		return builder;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		else if (obj instanceof HuntedClassSelector selector)
		{
			if (selector.classesByType.equals(this.classesByType))
				return true;
			else
				return false;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "[" + this.classesByType.toString() + "]";
	}
	
	public static class Builder
	{
		private final Map<HuntedClassType, HuntedClass> classesByType;
		
		private Builder()
		{
			this.classesByType = Maps.newHashMap();
		}
		
		public Builder setSelected(HuntedClassType type, HuntedClass huntedClass)
		{
			if (huntedClass.isMutable())
				throw new IllegalArgumentException("Hunted class must be immutable");
			this.classesByType.put(type, huntedClass);
			return this;
		}
		
		public boolean contains(HuntedClassType type)
		{
			return this.classesByType.containsKey(type);
		}
		
		public boolean hasSelected(HuntedClass huntedClass)
		{
			return this.classesByType.containsValue(huntedClass);
		}
		
		public HuntedClassSelector build()
		{
			return new HuntedClassSelector(ImmutableMap.copyOf(this.classesByType));
		}
	}
}
