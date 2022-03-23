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

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

public class BulletParticle extends SpriteTexturedParticle {
    public BulletParticle(ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, IAnimatedSprite spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.gravity = 0;
        this.lifetime = 30;
        this.hasPhysics = false;

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.pickSprite(spriteSet);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        this.move(this.xd, this.yd, this.zd);

        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        if (!state.getCollisionShape(level, pos).isEmpty() || onGround) {
            if (level.random.nextBoolean()) {
                RayTraceContext ctx = new RayTraceContext(new Vector3d(x, y, z), new Vector3d(x + xd, y + yd, z + zd), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);
                BlockRayTraceResult res = level.clip(ctx);
                if (res.getType() == RayTraceResult.Type.BLOCK) {
                    Direction face = res.getDirection();
                    level.addParticle(new BlockParticleData(ParticleTypes.BLOCK, state), x, y, z, face.getStepX() * 0.2, face.getStepY() * 0.2, face.getStepZ() * 0.2);
                }
            }
            remove();
        }
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(BasicParticleType data, ClientWorld level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new BulletParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }
}
