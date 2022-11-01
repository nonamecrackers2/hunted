package nonamecrackers2.hunted.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.hunted.init.HuntedCapabilities;

public class HuntedClassLoop extends AbstractTickableSoundInstance
{
	private final LivingEntity entity;
	
	public HuntedClassLoop(LivingEntity player, SoundEvent event)
	{
		super(event, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
		this.entity = player;
		this.looping = true;
		this.delay = 0;
	}
	
	@Override
	public void tick()
	{
		this.x = this.entity.getX();
		this.y = this.entity.getY();
		this.z = this.entity.getZ();
		
		this.entity.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (!manager.isInGame() || !this.entity.isAlive())
				this.stop();
		});
	}
	
	public LivingEntity getPlayer()
	{
		return this.entity;
	}
}
