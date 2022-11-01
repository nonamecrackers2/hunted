package nonamecrackers2.hunted.client.init;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.client.renderer.blockentity.KeyholeRenderer;
import nonamecrackers2.hunted.client.renderer.blockentity.KioskRenderer;
import nonamecrackers2.hunted.client.renderer.entity.HunterRenderer;
import nonamecrackers2.hunted.init.HuntedBlockEntityTypes;
import nonamecrackers2.hunted.init.HuntedEntityTypes;

public class HuntedRenderers
{
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(HuntedBlockEntityTypes.KEYHOLE.get(), KeyholeRenderer::new);
		event.registerBlockEntityRenderer(HuntedBlockEntityTypes.KIOSK.get(), KioskRenderer::new);
		
		event.registerEntityRenderer(HuntedEntityTypes.HUNTER.get(), HunterRenderer::new);
	}
}
