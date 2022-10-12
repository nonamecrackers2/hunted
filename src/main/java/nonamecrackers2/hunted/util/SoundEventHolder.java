package nonamecrackers2.hunted.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

public record SoundEventHolder(SoundEvent event, float pitch, float volume) 
{
	public static final Codec<SoundEventHolder> CODEC = RecordCodecBuilder.create(instance -> 
	{
		return instance.group(
				ForgeRegistries.SOUND_EVENTS.getCodec().fieldOf("type").forGetter(SoundEventHolder::event), 
				Codec.FLOAT.fieldOf("pitch").forGetter(SoundEventHolder::pitch), 
				Codec.FLOAT.fieldOf("volume").forGetter(SoundEventHolder::volume))
		.apply(instance, SoundEventHolder::new);
	});
	
	public void toPacket(FriendlyByteBuf buffer)
	{
		buffer.writeRegistryId(ForgeRegistries.SOUND_EVENTS, this.event);
		buffer.writeFloat(this.pitch);
		buffer.writeFloat(this.volume);
	}
	
	public static SoundEventHolder fromPacket(FriendlyByteBuf buffer)
	{
		SoundEvent event = buffer.readRegistryId();
		float pitch = buffer.readFloat();
		float volume = buffer.readFloat();
		return new SoundEventHolder(event, pitch, volume);
	}
}
