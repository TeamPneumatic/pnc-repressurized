/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.particle;

import me.desht.pneumaticcraft.common.particle.AirParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

public class AirParticle extends TextureSheetParticle {
    private final SpriteSet sprite;

    private AirParticle(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, float scale, SpriteSet sprite) {
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

        lifetime = 50;
        quadSize = scale;

        xd = xSpeedIn + worldIn.random.nextDouble() * 0.1 - 0.05;
        yd = ySpeedIn + worldIn.random.nextDouble() * 0.1 - 0.05;
        zd = zSpeedIn + worldIn.random.nextDouble() * 0.1 - 0.05;

        setSpriteFromAge(sprite);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isEmptyBlock(BlockPos.containing(x, y, z)) || onGround) {
            remove();
        }

        // fades out and gets bigger as it gets older
        setSpriteFromAge(sprite);
        scale(1.03f);
        alpha *= 0.975;

        if (level.random.nextInt(10) == 0) {
            xd += level.random.nextDouble() * 0.08 - 0.04;
        }
        if (level.random.nextInt(10) == 0) {
            yd += level.random.nextDouble() * 0.08 - 0.04;
        }
        if (level.random.nextInt(10) == 0) {
            yd += level.random.nextDouble() * 0.08 - 0.04;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
//        return AIR_PARTICLE_RENDER;
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<AirParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(AirParticleData airParticleData, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
            AirParticle p = new AirParticle(world, x, y, z, dx, dy, dz, 0.2f, spriteSet);
            p.setAlpha(airParticleData.getAlpha());
            return p;
        }
    }

//    private static final ParticleRenderType AIR_PARTICLE_RENDER = new ParticleRenderType() {
//        @Override
//        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
//            RenderSystem.depthMask(false);
//            RenderSystem.enableBlend();
//            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            // TODO 1.17 how do we do this now?
////            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.003921569F);
////            RenderSystem.disableLighting();
//
//            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
//            RenderSystem.setShader(GameRenderer::getParticleShader);
////            textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).setBlurMipmap(true, false);
//            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
//        }
//
//        @Override
//        public void end(Tesselator tessellator) {
//            tessellator.end();
//
//            Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
////            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
//            RenderSystem.disableBlend();
//            RenderSystem.depthMask(true);
//        }
//
//        @Override
//        public String toString() {
//            return "pneumaticcraft:air_particle";
//        }
//    };

}
