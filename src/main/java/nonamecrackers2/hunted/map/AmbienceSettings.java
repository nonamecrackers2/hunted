package nonamecrackers2.hunted.map;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistries;

public record AmbienceSettings(Optional<SoundEvent> background, Optional<SoundEvent> foreground, boolean flickeringLights)
{
	public void toPacket(FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(this.background.isPresent());
		this.background.ifPresent(sound -> buffer.writeRegistryId(ForgeRegistries.SOUND_EVENTS, sound));
		buffer.writeBoolean(this.foreground.isPresent());
		this.foreground.ifPresent(sound -> buffer.writeRegistryId(ForgeRegistries.SOUND_EVENTS, sound));
		buffer.writeBoolean(this.flickeringLights);
	}
	
	public static AmbienceSettings fromPacket(FriendlyByteBuf buffer)
	{
		Optional<SoundEvent> background = Optional.empty();
		if (buffer.readBoolean())
			background = Optional.of(buffer.readRegistryId());
		Optional<SoundEvent> foreground = Optional.empty();
		if (buffer.readBoolean())
			foreground = Optional.of(buffer.readRegistryId());
		boolean flickeringLights = buffer.readBoolean();
		return new AmbienceSettings(background, foreground, flickeringLights);
	}
	
	public static AmbienceSettings fromJson(JsonObject object)
	{
		SoundEvent background = null;
		if (object.has("background"))
			background = getSound(object, "background");
		SoundEvent foreground = null;
		if (object.has("foreground"))
			foreground = getSound(object, "foreground");
		boolean flickeringLights = false;
		if (object.has("flickering_lights"))
			flickeringLights = GsonHelper.getAsBoolean(object, "flickering_lights");
		return new AmbienceSettings(Optional.ofNullable(background), Optional.ofNullable(foreground), flickeringLights);
	}
	
	private static SoundEvent getSound(JsonObject object, String loc)
	{
		String rawId = GsonHelper.getAsString(object, loc);
		SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(rawId));
		if (event == null)
			throw new JsonSyntaxException("Unknown or unsupported sound event '" + rawId + "'");
		return event;
	}
}
