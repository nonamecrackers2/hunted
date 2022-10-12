package nonamecrackers2.hunted.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import nonamecrackers2.hunted.block.KeyholeBlock;
import nonamecrackers2.hunted.block.entity.KeyholeBlockEntity;

public class KeyholeRenderer implements BlockEntityRenderer<KeyholeBlockEntity>
{	
	private final ItemRenderer itemRenderer;
	
	public KeyholeRenderer(BlockEntityRendererProvider.Context context)
	{
		this.itemRenderer = context.getItemRenderer();
	}
	
	@Override
	public void render(KeyholeBlockEntity entity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight, int overlayTexture)
	{
		Direction direction = entity.getBlockState().getValue(KeyholeBlock.FACING);
		stack.pushPose();
		stack.translate(0.5D, 0.5D, 0.5D);
		stack.mulPose(Vector3f.YP.rotationDegrees(-direction.toYRot() - 90.0F));
		stack.translate(-0.7D, 0.0D, 0.0D);
		stack.mulPose(Vector3f.ZN.rotationDegrees(135.0F));
		int i = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(direction.getOpposite()));
		this.itemRenderer.renderStatic(entity.getItem(), ItemTransforms.TransformType.FIXED, i, overlayTexture, stack, buffer, (int)entity.getBlockPos().asLong());
		stack.popPose();
	}
}
