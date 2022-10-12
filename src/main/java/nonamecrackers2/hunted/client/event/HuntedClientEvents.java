package nonamecrackers2.hunted.client.event;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.block.entity.KeyholeBlockEntity;
import nonamecrackers2.hunted.client.keybind.HuntedKeybinds;
import nonamecrackers2.hunted.init.HuntedBlocks;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.HuntedPacketHandlers;
import nonamecrackers2.hunted.init.TriggerTypes;
import nonamecrackers2.hunted.packet.ActivateTriggerPacket;

public class HuntedClientEvents
{
	@SubscribeEvent
	public static void onKeymappingPressed(InputEvent.Key event)
	{
		Minecraft mc = Minecraft.getInstance();
		if (event.getAction() == InputConstants.PRESS && mc.screen == null)
		{
			if (event.getKey() == HuntedKeybinds.USE_ABILITY.getKey().getValue())
			{
				int id = 0;
				if (mc.crosshairPickEntity != null)
					id = mc.crosshairPickEntity.getId();
				HuntedPacketHandlers.MAIN.sendToServer(new ActivateTriggerPacket(InteractionHand.MAIN_HAND, TriggerTypes.KEYBIND.get(), id));
			}
		}
	}
	
	@SubscribeEvent
	public static void onRenderNametag(RenderNameTagEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		mc.player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).ifPresent(manager -> 
		{
			if (manager.isInGame())
				event.setResult(Result.DENY);
		});
	}
	
	@SubscribeEvent
	public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event)
	{
		if (event.getOverlay().equals(VanillaGuiOverlay.FOOD_LEVEL.type()) || event.getOverlay().equals(VanillaGuiOverlay.EXPERIENCE_BAR.type()) || event.getOverlay().equals(VanillaGuiOverlay.ARMOR_LEVEL.type()))
			event.setCanceled(true);
	}

	public static void registerBlockColors(RegisterColorHandlersEvent.Block event)
	{
		event.register((state, getter, pos, i) -> 
		{
			BlockEntity entity = getter.getBlockEntity(pos);
			if (entity instanceof KeyholeBlockEntity keyholeEntity)
				return keyholeEntity.getColor();
			else
				return 0;
		}, HuntedBlocks.KEYHOLE.get());
	}
}
