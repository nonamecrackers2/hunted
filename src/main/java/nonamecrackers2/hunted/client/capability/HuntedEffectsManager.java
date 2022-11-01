package nonamecrackers2.hunted.client.capability;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.client.init.HuntedClientCapabilities;
import nonamecrackers2.hunted.client.sound.MapLoop;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;

public class HuntedEffectsManager
{
	public static final ResourceLocation[] JUMPSCARES = new ResourceLocation[] {
			HuntedMod.resource("textures/jumpscare/jumpscare.png")
	};
	private static final int LIGHT_FADE = 20;
	private final Minecraft mc;
	private final RandomSource random = RandomSource.create();
	private int jumpscareTime;
	private int jumpscareTimeO;
	private Vec2 jumpscareShake = Vec2.ZERO;
	private Vec2 jumpscareShakeO = Vec2.ZERO;
	private @Nullable ResourceLocation jumpscare;
	private @Nullable MapLoop loop;
	private float vignetteAlpha;
	private float vignetteAlphaO;
	private int nextPulse = 20;
	private int pulseStart;
	private int pulseDuration;
	private float lightPulse;
	private float lightPulseO;
	private float lightFlicker;
	private float lightFlickerDuration;
	private int nextSound = 20 + this.random.nextInt(60);
	private int time;
	
	public HuntedEffectsManager(Minecraft mc)
	{
		this.mc = mc;
	}
	
	public void tick()
	{
		this.time++;
		
		this.jumpscareTimeO = this.jumpscareTime;
		this.jumpscareShakeO = this.jumpscareShake;
		this.lightPulseO = this.lightPulse;
		
		if (this.jumpscareTime > 0)
		{
			float x = Mth.cos(this.jumpscareTime * 4.5F) * 5F + (this.random.nextFloat() - 0.5F) * 5F;
			float z = Mth.sin(this.jumpscareTime * 3.5F) * 15F + (this.random.nextFloat() - 0.5F) * 2F;
			this.jumpscareShake = new Vec2(x, z);
			
			this.jumpscareTime--;
			if (this.jumpscareTime == 0)
			{
				this.jumpscare = null;
				this.jumpscareShake = Vec2.ZERO;
			}
		}
		
		if (this.loop != null && this.loop.isStopped())
			this.loop = null;
		
		this.mc.level.getCapability(HuntedClientCapabilities.GAME_INFO).ifPresent(info -> 
		{
			if (info.isGameRunning())
			{
				info.getMap().ifPresent(map -> 
				{
					map.ambience().ifPresent(ambience -> 
					{
						if (this.loop == null)
						{
							SoundEvent sound = ambience.background().orElse(null);
							if (sound != null)
							{
								this.loop = new MapLoop(sound);
								this.mc.getSoundManager().queueTickingSound(this.loop);
							}
						}
						
						if (ambience.flickeringLights())
						{
							if (this.nextPulse > 0)
							{
								this.nextPulse--;
								if (this.nextPulse == 0)
								{
									this.nextPulse = 200 + this.random.nextInt(800);
									this.pulseDuration = 80 + this.random.nextInt(60);
									this.lightFlickerDuration = 20 + this.random.nextInt(40);
									this.pulseStart = this.pulseDuration;
								}
							}
							
//							this.lightPulse = (float)Math.max(0.0D, Math.cos((this.time + 100) * 0.03F) - 0.4F);
//							System.out.println(this.lightPulse);
						}
						
						SoundEvent sound = ambience.foreground().orElse(null);
						if (sound != null)
						{
							if (this.nextSound > 0)
							{
								this.nextSound--;
								if (this.nextSound == 0)
								{
									this.nextSound = 100 + this.random.nextInt(1200);
									double x = this.mc.player.getRandomX(20.0D);
									double z = this.mc.player.getRandomZ(20.0D);
									this.mc.level.playLocalSound(x, this.mc.player.getY(), z, sound, SoundSource.AMBIENT, 50.0F, 1.0F, false);
								}
							}
						}
					});
				});
			}
		});
		
		if (this.pulseDuration > 0)
			this.pulseDuration--;
		
		if (this.lightFlickerDuration > 0)
		{
			this.lightFlickerDuration--;
			this.lightFlicker = Mth.cos(this.lightFlickerDuration + this.random.nextInt(20)) * Mth.sin(this.lightFlickerDuration + 30 + this.random.nextInt(20)) * 0.2F;
			if (this.lightFlickerDuration == 0)
				this.lightFlicker = 0.0F;
		}
		
		this.lightPulse = Math.max(0.0F, (Math.min(1.0F, ((float)LIGHT_FADE - (float)(this.pulseDuration - (this.pulseStart - LIGHT_FADE))) / (float)LIGHT_FADE) * Math.min(1.0F, (float)this.pulseDuration / (float)LIGHT_FADE) - this.lightFlicker));
		
		this.vignetteAlphaO = this.vignetteAlpha;
		this.mc.player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (!manager.hasEscaped() && manager.isInGame())
			{
				var distance = distanceFromNearestDanger(this.mc).orElse(null);
				if (distance != null)
				{
					float multiplier = (float)Math.max(0.0D, 10.0D - distance + 5.0D);
					this.vignetteAlpha = Math.min(1.0F, multiplier * 0.1F) * 0.8F;
					this.vignetteAlpha += Math.max(0.0D, Math.sin(this.time * 0.1F) - 0.2F) * 0.2F;
				}
				else
				{
					this.vignetteAlpha = 0.0F;
				}
			}
			else
			{
				this.vignetteAlpha = 0.0F;
			}
		});
	}
	
	public void doJumpscare(SoundEvent sound, int time)
	{
		this.jumpscareTime = time;
		this.jumpscare = JUMPSCARES[this.random.nextInt(JUMPSCARES.length)];
		this.mc.player.playNotifySound(sound, SoundSource.HOSTILE, 1.0F, 1.0F);
		this.pulseDuration = 180;
		this.pulseStart = 180;
		this.lightFlickerDuration = 120;
	}
	
	public int getJumpscareTime()
	{
		return this.jumpscareTime;
	}
	
	public float getJumpscareTime(float partialTicks)
	{
		return Mth.lerp(partialTicks, this.jumpscareTimeO, this.jumpscareTime);
	}
	
	public Vec2 getJumpscareShake()
	{
		return this.jumpscareShake;
	}
	
	public Vec2 getJumpscareShakeO()
	{
		return this.jumpscareShakeO;
	}
	
	public @Nullable ResourceLocation getJumpscare()
	{
		return this.jumpscare;
	}
	
	public float getVignetteAlpha(float partialTicks)
	{
		return Mth.lerp(partialTicks, this.vignetteAlphaO, this.vignetteAlpha);
	}
	
	public float getLightPulse(float partialTicks)
	{
		return Mth.lerp(partialTicks, this.lightPulseO, this.lightPulse);
	}
	
	public static Optional<Double> distanceFromNearestDanger(Minecraft mc)
	{
		for (Entity entity : mc.level.entitiesForRendering())
		{
			var manager = entity.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
			if (manager != null && manager.isInGame())
			{
				HuntedClass huntedClass = manager.getCurrentClass().orElse(null);
				if (huntedClass != null && huntedClass.getType().isDangerous() && !mc.player.equals(entity))
					return Optional.of(mc.gameRenderer.getMainCamera().getPosition().distanceTo(entity.getEyePosition()));
			}
		}
		return Optional.empty();
	}
	
	public static class Events
	{
		@SubscribeEvent
		public static void tickAmbience(TickEvent.ClientTickEvent event)
		{
			Minecraft mc = Minecraft.getInstance();
			if (mc.level != null && !mc.isPaused())
			{
				mc.level.getCapability(HuntedClientCapabilities.EFFECTS_MANAGER).ifPresent(manager -> {
					manager.tick();
				});
			}
		}
	}
}
