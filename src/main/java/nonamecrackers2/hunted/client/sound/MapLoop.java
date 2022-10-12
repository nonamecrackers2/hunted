package nonamecrackers2.hunted.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import nonamecrackers2.hunted.client.capability.HuntedEffectsManager;
import nonamecrackers2.hunted.client.init.HuntedClientCapabilities;

public class MapLoop extends AbstractTickableSoundInstance
{
	public MapLoop(SoundEvent event)
	{
		super(event, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
		this.looping = true;
		this.delay = 0;
	}
	
	@Override
	public void tick()
	{
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		this.x = player.getX();
		this.y = player.getY();
		this.z = player.getZ();
		
		mc.level.getCapability(HuntedClientCapabilities.GAME_INFO).ifPresent(info -> 
		{
			if (!info.isGameRunning())
				this.stop();
		});
		
		var distance = HuntedEffectsManager.distanceFromNearestDanger(mc).orElse(null);
		if (distance != null)
			this.volume = Mth.clamp((float)(Math.max(0.0D, distance - 5.0D) * 0.1D), 0.2F, 1.0F);
		else
			this.volume = 1.0F;
	}
}
