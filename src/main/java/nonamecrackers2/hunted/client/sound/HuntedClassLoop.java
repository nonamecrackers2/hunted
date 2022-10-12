package nonamecrackers2.hunted.client.sound;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import nonamecrackers2.hunted.init.HuntedCapabilities;

public class HuntedClassLoop extends AbstractTickableSoundInstance
{
	private final AbstractClientPlayer player;
	
	public HuntedClassLoop(AbstractClientPlayer player, SoundEvent event)
	{
		super(event, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
		this.player = player;
		this.looping = true;
		this.delay = 0;
	}
	
	@Override
	public void tick()
	{
		this.x = this.player.getX();
		this.y = this.player.getY();
		this.z = this.player.getZ();
		
		this.player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (!manager.isInGame() || !this.player.isAlive())
				this.stop();
		});
	}
	
	public AbstractClientPlayer getPlayer()
	{
		return this.player;
	}
}
