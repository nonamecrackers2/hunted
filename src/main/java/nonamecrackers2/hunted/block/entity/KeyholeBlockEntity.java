package nonamecrackers2.hunted.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import nonamecrackers2.hunted.init.HuntedBlockEntityTypes;

public class KeyholeBlockEntity extends BlockEntity
{
	private ItemStack item = ItemStack.EMPTY;
	private int color;
	
	public KeyholeBlockEntity(BlockPos pos, BlockState state)
	{
		super(HuntedBlockEntityTypes.KEYHOLE.get(), pos, state);
	}
	
	@Override
	protected void saveAdditional(CompoundTag tag)
	{
		super.saveAdditional(tag);
		tag.putInt("Color", this.color);
		tag.put("Item", this.getItem().save(new CompoundTag()));
	}
	
	@Override
	public void load(CompoundTag tag)
	{
		super.load(tag);
		if (this.level != null && this.level.isClientSide && this.color != tag.getInt("Color"))
			this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
		this.color = tag.getInt("Color");
		this.setItem(ItemStack.of(tag.getCompound("Item")));
	}
	
	@Override
	public CompoundTag getUpdateTag()
	{
		return this.saveWithoutMetadata();
	}
	
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	public int getColor()
	{
		return this.color;
	}
	
	public void setColor(int color)
	{
		this.color = color;
	}

	public ItemStack getItem()
	{
		return this.item;
	}
	
	public void setItem(ItemStack stack)
	{
		this.item = stack;
		this.setChanged();
		if (this.level != null && !this.level.isClientSide)
			this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
	}
	
	public boolean isEmpty()
	{
		return this.item.isEmpty();
	}
	
//	@Override
//	public int getContainerSize()
//	{
//		return this.items.size();
//	}
//
//	public boolean isEmpty()
//	{
//		for (ItemStack stack : this.items)
//		{
//			if (!stack.isEmpty())
//				return false;
//		}
//		return true;
//	}
//
//	public ItemStack getItem(int index)
//	{
//		return index >= 0 && index < this.items.size() ? this.items.get(index) : ItemStack.EMPTY;
//	}
//
//	public ItemStack removeItem(int p_18942_, int p_18943_)
//	{
//		return ContainerHelper.removeItem(this.items, p_18942_, p_18943_);
//	}
//
//	public ItemStack removeItemNoUpdate(int index)
//	{
//		return ContainerHelper.takeItem(this.items, index);
//	}
//
//	public void setItem(int index, ItemStack stack)
//	{
//		if (index >= 0 && index < this.items.size())
//			this.items.set(index, stack);
//	}
//
//	public void clearContent()
//	{
//		this.items.clear();
//	}
}
