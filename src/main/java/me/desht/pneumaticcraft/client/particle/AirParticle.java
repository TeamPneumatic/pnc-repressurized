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
        alpha *= 0.975f;

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
            p.setAlpha(airParticleData.alpha());
            return p;
        }
    }
}
