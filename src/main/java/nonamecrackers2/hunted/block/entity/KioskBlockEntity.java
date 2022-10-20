package nonamecrackers2.hunted.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import nonamecrackers2.hunted.init.HuntedBlockEntityTypes;
import nonamecrackers2.hunted.menu.KioskMenu;

public class KioskBlockEntity extends BlockEntity implements MenuProvider
{
	public int time;
	public float flip;
	public float flipO;
	public float flipT;
	public float flipA;
	public float open;
	public float openO;
	public float rot;
	public float rotO;
	public float rotT;
	public final RandomSource random = RandomSource.create();
	
	public KioskBlockEntity(BlockPos pos, BlockState state)
	{
		super(HuntedBlockEntityTypes.KIOSK.get(), pos, state);
	}
	
	public static void tick(Level level, BlockPos pos, BlockState state, KioskBlockEntity entity)
	{
		entity.openO = entity.open;
		entity.rotO = entity.rot;
		Player player = level.getNearestPlayer((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, 3.0D, false);
		if (player != null)
		{
			double d0 = player.getX() - ((double)pos.getX() + 0.5D);
			double d1 = player.getZ() - ((double)pos.getZ() + 0.5D);
			entity.rotT = (float) Mth.atan2(d1, d0);
			entity.open += 0.1F;
			if (entity.open < 0.5F || entity.random.nextInt(40) == 0)
			{
				float f1 = entity.flipT;

				do
					entity.flipT += (float)(entity.random.nextInt(4) - entity.random.nextInt(4));
				while (f1 == entity.flipT);
			}
		}
		else
		{
			entity.rotT += 0.02F;
			entity.open -= 0.1F;
		}
		while (entity.rot >= (float)Math.PI)
			entity.rot -= ((float)Math.PI * 2F);
		while (entity.rot < -(float)Math.PI)
			entity.rot += ((float)Math.PI * 2F);
		while (entity.rotT >= (float)Math.PI)
			entity.rotT -= ((float)Math.PI * 2F);
		while (entity.rotT < -(float)Math.PI)
			entity.rotT += ((float)Math.PI * 2F);
		float f2;
		for (f2 = entity.rotT - entity.rot; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F)) {}
		while (f2 < -(float)Math.PI)
			f2 += ((float)Math.PI * 2F);
		entity.rot += f2 * 0.4F;
		entity.open = Mth.clamp(entity.open, 0.0F, 1.0F);
		entity.time++;
		entity.flipO = entity.flip;
		float f = (entity.flipT - entity.flip) * 0.4F;
		f = Mth.clamp(f, -0.2F, 0.2F);
		entity.flipA += (f - entity.flipA) * 0.9F;
		entity.flip += entity.flipA;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player)
	{
		return new KioskMenu(id, inventory);
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("hunted.menu.game.title");
	}
}
