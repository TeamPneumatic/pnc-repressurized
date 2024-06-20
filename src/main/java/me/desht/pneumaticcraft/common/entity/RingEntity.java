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
import me.desht.pneumaticcraft.common.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RingEntity extends Entity {
    public ProgressingLine ring, oldRing;
    private final Entity targetEntity;
    public final int color;

    public RingEntity(EntityType<RingEntity> type, Level world) {
        super(type, world);

        targetEntity = null;
        color = 0;
    }

    public RingEntity(Level par1World, double startX, double startY, double startZ, Entity targetEntity, int color) {
        super(ModEntityTypes.RING.get(), par1World);

        setPos(startX, startY, startZ);
        xOld = startX;
        yOld = startY;
        zOld = startZ;
        this.targetEntity = targetEntity;
        this.color = color;

        double dx = targetEntity.getX() - getX();
        double dy = targetEntity.getY() - getY();
        double dz = targetEntity.getZ() - getZ();
        float f = Mth.sqrt((float) (dx * dx + dz * dz));
        setYRot((float) (Math.atan2(dx, dz) * 180.0D / Math.PI));
        setXRot((float) (Math.atan2(dy, f) * 180.0D / Math.PI));
        yRotO = getYRot();
        xRotO = getXRot();
        noCulling = true;
        if (par1World.isClientSide) {
            setViewScale(10.0D);
        }
    }

    @Override
    public void tick() {
        if (targetEntity == null) return;

        Vec3 end = targetEntity.position();
        yRotO = getYRot();
        xRotO = getXRot();

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
            double f = Math.sqrt(dx * dx + dz * dz);
            setYRot((float) (Math.atan2(dx, dz) * 180.0D / Math.PI));
            setXRot((float) (Math.atan2(dy, f) * 180.0D / Math.PI));

            oldRing.setProgress(ring.getProgress());
            if (ring.incProgress(0.05F)) {
                discard();
            }
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // none
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

}
