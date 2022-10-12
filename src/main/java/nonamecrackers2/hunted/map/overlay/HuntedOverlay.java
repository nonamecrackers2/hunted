package nonamecrackers2.hunted.map.overlay;

import java.util.List;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.util.HuntedUtil;

public abstract class HuntedOverlay<T>
{
	private final Codec<T> codec;
	
	protected HuntedOverlay(Codec<T> codec)
	{
		this.codec = codec;
	}
	
	public HuntedOverlay.ConfiguredOverlay<T> configure(JsonElement element)
	{
		return new HuntedOverlay.ConfiguredOverlay<>(this, this.codec.parse(JsonOps.INSTANCE, element).resultOrPartial(HuntedUtil::throwJSE).get());
	}
	
	public abstract List<Component> getText(T settings, ServerLevel level, HuntedGame game);
	
	public static record ConfiguredOverlay<T>(HuntedOverlay<T> overlay, T settings)
	{
		public List<Component> getText(ServerLevel level, HuntedGame game)
		{
			return this.overlay.getText(this.settings, level, game);
		}
	}
}
