package nonamecrackers2.hunted.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.SimpleParticleType;

public class HighlightParticle extends SimpleAnimatedParticle
{
	public HighlightParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites)
	{
		super(level, x, y, z, sprites, 0.0125F);
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.lifetime = 60 + this.random.nextInt(12);
		this.setFadeColor(15916745);
		this.setSpriteFromAge(sprites);
	}
	
	@Override
	public void move(double x, double y, double z)
	{
		this.setBoundingBox(this.getBoundingBox().move(x, y, z));
		this.setLocationFromBoundingbox();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(VertexConsumer consumer, Camera camera, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		OutlineBufferSource buffer = mc.renderBuffers().outlineBufferSource();
		buffer.setColor(255, 255, 255, 255);
		super.render(buffer.getBuffer(RenderType.outline(TextureAtlas.LOCATION_PARTICLES)), camera, partialTicks);
		buffer.endOutlineBatch();
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType>
	{
		private final SpriteSet sprites;
		
		public Provider(SpriteSet sprites)
		{
			this.sprites = sprites;
		}
		
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) 
		{
			return new HighlightParticle(level, x, y, z, xd, yd, zd, this.sprites);
		}
	}
}
