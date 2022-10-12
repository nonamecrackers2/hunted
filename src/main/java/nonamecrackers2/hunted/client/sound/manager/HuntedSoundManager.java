package nonamecrackers2.hunted.client.sound.manager;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.client.init.HuntedClientCapabilities;
import nonamecrackers2.hunted.client.sound.HuntedClassLoop;
import nonamecrackers2.hunted.init.HuntedCapabilities;

public class HuntedSoundManager
{
	private final Minecraft mc;
	private final Int2ObjectMap<HuntedClassLoop> classLoops;
	
	public HuntedSoundManager(Minecraft mc)
	{
		this.mc = mc;
		this.classLoops = new Int2ObjectOpenHashMap<>();;
	}
	
	public void tick()
	{
		var iterator = this.classLoops.int2ObjectEntrySet().iterator();
		while (iterator.hasNext())
		{
			HuntedClassLoop loop = iterator.next().getValue();
			if (loop.isStopped())
				iterator.remove();
		}
		
		for (AbstractClientPlayer player : this.mc.level.players())
		{
			if (!player.equals(this.mc.player))
			{
				player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
				{
					if (manager.isInGame())
					{
						manager.getCurrentClass().ifPresent(huntedClass -> 
						{
							SoundEvent event = huntedClass.getLoopSound().orElse(null);
							if (event != null && !this.classLoops.containsKey(player.getId()))
							{
								HuntedClassLoop loop = new HuntedClassLoop(player, event);
								this.classLoops.put(player.getId(), loop);
								this.mc.getSoundManager().queueTickingSound(loop);
							}
						});
					}
				});
			}
		}
	}
	
	public static class Events
	{
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event)
		{
			Minecraft mc = Minecraft.getInstance();
			if (!mc.isPaused() && mc.level != null)
			{
				mc.level.getCapability(HuntedClientCapabilities.SOUND_MANAGER).ifPresent(manager ->{
					manager.tick();
				});
			}
		}
	}
}
