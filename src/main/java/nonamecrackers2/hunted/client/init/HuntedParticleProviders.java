package nonamecrackers2.hunted.client.init;

import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import nonamecrackers2.hunted.client.particle.HighlightParticle;
import nonamecrackers2.hunted.init.HuntedParticleTypes;

public class HuntedParticleProviders
{
	public static void registerProviders(RegisterParticleProvidersEvent event)
	{
		event.register(HuntedParticleTypes.HIGHLIGHT.get(), HighlightParticle.Provider::new);
	}
}
