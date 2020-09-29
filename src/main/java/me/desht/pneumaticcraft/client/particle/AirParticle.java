package me.desht.pneumaticcraft.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class AirParticle extends SpriteTexturedParticle {
    private final IAnimatedSprite sprite;

    private AirParticle(ClientWorld worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, float scale, IAnimatedSprite sprite) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);

        this.sprite = sprite;
//        if (sprite == null) {
//            Calendar calendar = Calendar.getInstance();
//            if (calendar.get(Calendar.MONTH) == Calendar.MARCH && calendar.get(Calendar.DAY_OF_MONTH) >= 31
//                || calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) <= 2) {
//                sprite = Minecraft.getInstance().getTextureMap().getAtlasSprite(AIR_PARTICLE_TEXTURE2.toString());
//            } else {
//                sprite = Minecraft.getInstance().getTextureMap().getAtlasSprite(AIR_PARTICLE_TEXTURE.toString());
//            }
//        }

        maxAge = 50;
        particleScale = scale;

        motionX = xSpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;
        motionY = ySpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;
        motionZ = zSpeedIn + worldIn.rand.nextDouble() * 0.1 - 0.05;

        selectSpriteWithAge(sprite);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isAirBlock(new BlockPos(posX, posY, posZ)) || onGround) {
            setExpired();
        }

        // fades out and gets bigger as it gets older
        selectSpriteWithAge(sprite);
        multiplyParticleScaleBy(1.03f);
        particleAlpha *= 0.975;

        if (world.rand.nextInt(5) == 0) {
            motionX += world.rand.nextDouble() * 0.1 - 0.05;
        }
        if (world.rand.nextInt(5) == 0) {
            motionY += world.rand.nextDouble() * 0.1 - 0.05;
        }
        if (world.rand.nextInt(5) == 0) {
            motionY += world.rand.nextDouble() * 0.1 - 0.05;
        }
    }

    @Override
    public IParticleRenderType getRenderType() {
        return AIR_PARTICLE_RENDER;
    }

    public static class Factory implements IParticleFactory<AirParticleData> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle makeParticle(AirParticleData airParticleData, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
            AirParticle p = new AirParticle(world, x, y, z, dx, dy, dz, 0.2f, spriteSet);
            p.setAlphaF(airParticleData.getAlpha());
            return p;
        }
    }

    private static final IParticleRenderType AIR_PARTICLE_RENDER = new IParticleRenderType() {
        @Override
        public void beginRender(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            RenderSystem.disableLighting();

            textureManager.bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
            textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE).setBlurMipmap(true, false);
            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        }

        @Override
        public void finishRender(Tessellator tessellator) {
            tessellator.draw();

            Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE).restoreLastBlurMipmap();
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "pneumaticcraft:air_particle";
        }
    };

}
