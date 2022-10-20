package nonamecrackers2.hunted.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.block.entity.KioskBlockEntity;

public class KioskRenderer implements BlockEntityRenderer<KioskBlockEntity>
{	
	private static final ResourceLocation BOOK_LOCATION = HuntedMod.resource("textures/block/kiosk_book.png");
	private final BookModel bookModel;
	
	public KioskRenderer(BlockEntityRendererProvider.Context context)
	{
		this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
	}
	
	@Override
	public void render(KioskBlockEntity entity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight, int overlayTexture)
	{
		stack.pushPose();
		stack.translate(0.5D, 1.2D, 0.5D);
		float time = (float)entity.time + partialTicks;
		float yRot = entity.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot();
		stack.mulPose(Vector3f.YN.rotationDegrees(yRot));
		stack.translate(0.0D, (double)(0.1F + Mth.sin(time * 0.1F) * 0.1F), -0.2D);
		float f1;
		for (f1 = entity.rot - entity.rotO; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) {}
		while (f1 < -(float) Math.PI)
			f1 += ((float) Math.PI * 2F);
		float f2 = entity.rotO + f1 * partialTicks - yRot * ((float)Math.PI / 180.0F);
		stack.mulPose(Vector3f.YP.rotation(-f2));
		stack.mulPose(Vector3f.ZP.rotationDegrees(50.0F));
		float f3 = Mth.lerp(partialTicks, entity.flipO, entity.flip);
		float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
		float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
		float f6 = Mth.lerp(partialTicks, entity.openO, entity.open);
		this.bookModel.setupAnim(time, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
		VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entitySolid(BOOK_LOCATION));
		this.bookModel.render(stack, vertexconsumer, packedLight, overlayTexture, 1.0F, 1.0F, 1.0F, 1.0F);
		stack.popPose();
	}
}
