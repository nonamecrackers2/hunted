package nonamecrackers2.hunted.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import nonamecrackers2.hunted.capability.PlayerClassManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.util.EmptyFoodData;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity
{
	private MixinPlayer(EntityType<? extends LivingEntity> type, Level level)
	{
		super(type, level);
	}

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	public void constructorTail(Level level, BlockPos pos, float rot, GameProfile profile, CallbackInfo ci)
	{
		this.setFoodData(new EmptyFoodData());
	}
	
	@Accessor
	public abstract void setFoodData(FoodData data);
	
	@Inject(
		method = "mayBuild",
		at = @At("HEAD"),
		cancellable = true
	)
	public void mayBuildHead(CallbackInfoReturnable<Boolean> ci)
	{
		Player self = (Player)(Object)this;
		PlayerClassManager manager = this.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
		if (manager != null && manager.isInGame() || !self.isCreative())
			ci.setReturnValue(false);
	}
	
	@Inject(
		method = "blockActionRestricted",
		at = @At("HEAD"),
		cancellable = true
	)
	public void blockActionRestrictedHead(Level level, BlockPos pos, GameType type, CallbackInfoReturnable<Boolean> ci)
	{
		Player self = (Player)(Object)this;
		PlayerClassManager manager = this.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
		if (manager != null && manager.isInGame() || !self.isCreative())
			ci.setReturnValue(true);
	}
	
	@Override
	public int getTeamColor()
	{
		PlayerClassManager manager = this.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
		if (manager != null)
		{
			HuntedClass huntedClass = manager.getCurrentClass().orElse(null);
			if (huntedClass != null)
				return huntedClass.getType().getColor();
		}
		return super.getTeamColor();
	}
}
