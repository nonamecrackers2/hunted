package nonamecrackers2.hunted.util;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;

public interface DataHolder
{
	@Nullable CompoundTag getTag();
	
	void setTag(CompoundTag tag);
	
	default CompoundTag getOrCreateTag()
	{
		if (this.getTag() == null)
			this.setTag(new CompoundTag());
		return this.getTag();
	}
	
	default CompoundTag getOrCreateTagElement(String id)
	{
		return HuntedUtil.getOrCreateTagElement(this.getOrCreateTag(), id);
	}
}
