package nonamecrackers2.hunted.map.event;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;
import nonamecrackers2.hunted.util.SoundEventHolder;
import nonamecrackers2.hunted.util.TargetSupplier;

public class PlaySoundEvent extends MapEvent<PlaySoundEvent.Settings>
{
	public static final Codec<PlaySoundEvent.Settings> CODEC = RecordCodecBuilder.create(instance -> 
	{
		return instance.group(TargetSupplier.CODEC.optionalFieldOf("target").forGetter(s -> Optional.of(s.supplier)),
				SoundEventHolder.CODEC.fieldOf("sound").forGetter(PlaySoundEvent.Settings::sound))
				.apply(instance, (target, supplier) -> new PlaySoundEvent.Settings(target.orElse(TargetSupplier.DEFAULT), supplier));
	});
	
	public PlaySoundEvent()
	{
		super(CODEC);
	}
	
	@Override
	public void activate(PlaySoundEvent.Settings settings, TriggerContext context, CompoundTag tag)
	{
		for (ServerPlayer player : settings.supplier().getPlayers(context))
			player.playNotifySound(settings.sound().event(), SoundSource.PLAYERS, settings.sound().volume(), settings.sound().pitch());
	}
	
	@Override
	public Trigger.Criteria triggerCriteria(PlaySoundEvent.Settings settings)
	{
		return settings.supplier().getTriggerCriteria();
	}
	
	protected static record Settings(TargetSupplier supplier, SoundEventHolder sound) {}
}
