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

        if (!level.isEmptyBlock(new BlockPos(x, y, z)) || onGround) {
            remove();
        }

        // fades out and gets bigger as it gets older
        setSpriteFromAge(sprite);
        scale(1.03f);
        alpha *= 0.975;

        if (level.random.nextInt(5) == 0) {
            xd += level.random.nextDouble() * 0.1 - 0.05;
        }
        if (level.random.nextInt(5) == 0) {
            yd += level.random.nextDouble() * 0.1 - 0.05;
        }
        if (level.random.nextInt(5) == 0) {
            yd += level.random.nextDouble() * 0.1 - 0.05;
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
        public Particle createParticle(AirParticleData airParticleData, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
            AirParticle p = new AirParticle(world, x, y, z, dx, dy, dz, 0.2f, spriteSet);
            p.setAlpha(airParticleData.getAlpha());
            return p;
        }
    }

    private static final IParticleRenderType AIR_PARTICLE_RENDER = new IParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            RenderSystem.disableLighting();

            textureManager.bind(AtlasTexture.LOCATION_PARTICLES);
            textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES).setBlurMipmap(true, false);
            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE);
        }

        @Override
        public void end(Tessellator tessellator) {
            tessellator.end();

            Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES).restoreLastBlurMipmap();
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
