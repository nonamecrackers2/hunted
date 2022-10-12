package nonamecrackers2.hunted.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EmptyFoodData extends FoodData
{
	@Override
	public void eat(int food, float saturation)
	{
	}
	
	@Override
	public void eat(Item item, ItemStack stack)
	{
	}
	
	@Override
	public void eat(Item item, ItemStack stack, @Nullable LivingEntity entity)
	{
	}
	
	@Override
	public void tick(Player player)
	{
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag tag)
	{
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag tag)
	{
	}
	
	@Override
	public int getFoodLevel()
	{
		return 20;
	}
	
	@Override
	public int getLastFoodLevel()
	{
		return 20;
	}
	
	@Override
	public boolean needsFood()
	{
		return false;
	}
	
	@Override
	public void addExhaustion(float amount)
	{
	}
	
	@Override
	public float getExhaustionLevel()
	{
		return 0.0F;
	}
	
	@Override
	public float getSaturationLevel()
	{
		return 20.0F;
	}
	
	@Override
	public void setFoodLevel(int amount)
	{
	}
	
	@Override
	public void setSaturation(float p_38718_)
	{
	}
	
	@Override
	public void setExhaustion(float p_150379_)
	{
	}
}
