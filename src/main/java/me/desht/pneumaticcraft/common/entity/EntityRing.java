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

package me.desht.pneumaticcraft.common.entity;

import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.common.core.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityRing extends Entity {
    public ProgressingLine ring, oldRing;
    private final Entity targetEntity;
    public final int color;

    public EntityRing(EntityType<EntityRing> type, World world) {
        super(type, world);

        targetEntity = null;
        color = 0;
    }

    public EntityRing(World par1World, double startX, double startY, double startZ, Entity targetEntity, int color) {
        super(ModEntities.RING.get(), par1World);

        setPos(startX, startY, startZ);
        xOld = startX;
        yOld = startY;
        zOld = startZ;
        this.targetEntity = targetEntity;
        this.color = color;

        double dx = targetEntity.getX() - getX();
        double dy = targetEntity.getY() - getY();
        double dz = targetEntity.getZ() - getZ();
        float f = MathHelper.sqrt(dx * dx + dz * dz);
        yRotO = yRot = (float) (Math.atan2(dx, dz) * 180.0D / Math.PI);
        xRotO = xRot = (float) (Math.atan2(dy, f) * 180.0D / Math.PI);
        noCulling = true;
        if (par1World.isClientSide) {
            setViewScale(10.0D);
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        if (targetEntity == null) return;

        Vector3d end = targetEntity.position();
        yRotO = yRot;
        xRotO = xRot;

        if (ring == null) {
            ring = new ProgressingLine(position(), end);
        } else {
            if (oldRing == null) {
                oldRing = new ProgressingLine(ring.startX, ring.startY, ring.startZ, ring.endX, ring.endY, ring.endZ);
            } else {
                oldRing.endX = ring.endX;
                oldRing.endY = ring.endY;
                oldRing.endZ = ring.endZ;
            }
            ring.endX = (float) end.x;
            ring.endY = (float) end.y;
            ring.endZ = (float) end.z;

            double dx = end.x - getX();
            double dy = end.y - getY();
            double dz = end.z - getZ();
            float f = MathHelper.sqrt(dx * dx + dz * dz);
            yRot = (float) (Math.atan2(dx, dz) * 180.0D / Math.PI);
            xRot = (float) (Math.atan2(dy, f) * 180.0D / Math.PI);

            oldRing.setProgress(ring.getProgress());
            if (ring.incProgress(0.05F)) {
                remove();
            }
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
    }

}
