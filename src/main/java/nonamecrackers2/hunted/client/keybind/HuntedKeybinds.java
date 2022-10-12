package nonamecrackers2.hunted.client.keybind;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

public class HuntedKeybinds
{
	public static final KeyMapping USE_ABILITY = new KeyMapping("hunted.key.useAbility", GLFW.GLFW_KEY_R, "hunted.key.categories.hunted");
	
	public static void registerKeymappings(RegisterKeyMappingsEvent event)
	{
		event.register(USE_ABILITY);
	}
}
