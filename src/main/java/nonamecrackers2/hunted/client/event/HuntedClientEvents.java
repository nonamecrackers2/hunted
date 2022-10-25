package nonamecrackers2.hunted.client.event;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.hunted.block.entity.KeyholeBlockEntity;
import nonamecrackers2.hunted.client.init.HuntedClientCapabilities;
import nonamecrackers2.hunted.client.keybind.HuntedKeybinds;
import nonamecrackers2.hunted.init.HuntedBlocks;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.HuntedPacketHandlers;
import nonamecrackers2.hunted.init.HuntedParticleTypes;
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
	
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && !mc.isPaused())
		{
			mc.level.getCapability(HuntedClientCapabilities.GAME_INFO).ifPresent(info -> 
			{
				if (info.isGameRunning() && info.buttonHighlighting())
				{
					info.getMap().ifPresent(map -> 
					{
						for (BlockPos pos : map.buttons())
						{
							Vec3 vec = Vec3.atCenterOf(pos);
							if (Math.sqrt(mc.player.distanceToSqr(vec)) <= 16.0D && mc.player.tickCount % 20 == 0)
								mc.level.addAlwaysVisibleParticle(HuntedParticleTypes.HIGHLIGHT.get(), vec.x, vec.y, vec.z, mc.player.getRandom().nextGaussian() * 0.01, mc.player.getRandom().nextGaussian() * 0.01, mc.player.getRandom().nextGaussian() * 0.01);
						}
					});
				}
			});
		}
	}
}
