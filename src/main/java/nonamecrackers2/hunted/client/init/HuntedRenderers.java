package nonamecrackers2.hunted.client.init;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.client.renderer.blockentity.KeyholeRenderer;
import nonamecrackers2.hunted.init.HuntedBlockEntityTypes;

public class HuntedRenderers
{
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(HuntedBlockEntityTypes.KEYHOLE.get(), KeyholeRenderer::new);
	}
}
