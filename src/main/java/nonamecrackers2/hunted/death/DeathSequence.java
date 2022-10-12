package nonamecrackers2.hunted.death;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.util.HuntedUtil;

public abstract class DeathSequence<T>
{
	private final Codec<T> codec;
	
	public DeathSequence(Codec<T> codec)
	{
		this.codec = codec;
	}
	
	protected abstract void runSequence(T settings, ServerLevel level, ServerPlayer player, HuntedClass huntedClass, HuntedGame game, CompoundTag tag);
	
	protected void tick(T settings, ServerLevel level, ServerPlayer player, HuntedClass huntedClass, HuntedGame game, CompoundTag tag) {}
	
	public ConfiguredDeathSequence<T> configure(JsonElement element)
	{
		return new ConfiguredDeathSequence<>(this, this.codec.parse(JsonOps.INSTANCE, element).resultOrPartial(HuntedUtil::throwJSE).get());
	}
	
	public static record ConfiguredDeathSequence<T>(DeathSequence<T> type, T settings)
	{
		public void runSequence(ServerLevel level, ServerPlayer player, HuntedClass huntedClass, HuntedGame game, CompoundTag tag)
		{
			this.type.runSequence(this.settings, level, player, huntedClass, game, tag);
		}
		
		public void tick(ServerLevel level, ServerPlayer player, HuntedClass huntedClass, HuntedGame game, CompoundTag tag)
		{
			this.type.tick(this.settings, level, player, huntedClass, game, tag);
		}
	}
}
