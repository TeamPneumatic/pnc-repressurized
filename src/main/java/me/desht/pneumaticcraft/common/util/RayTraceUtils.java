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

package me.desht.pneumaticcraft.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public class RayTraceUtils {
    public static RayTraceResult getEntityLookedObject(LivingEntity entity, double maxDistance) {
        Pair<Vector3d, Vector3d> vecs = getStartAndEndLookVec(entity, maxDistance);
        RayTraceContext ctx = new RayTraceContext(vecs.getLeft(), vecs.getRight(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity);
        return entity.level.clip(ctx);
    }

    public static Pair<Vector3d, Vector3d> getStartAndEndLookVec(LivingEntity entity, double maxDistance) {
        Vector3d entityVec = new Vector3d(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        Vector3d maxDistVec = entityVec.add(entity.getViewVector(1F).scale(maxDistance));
        return new ImmutablePair<>(entity.getEyePosition(1F), maxDistVec);
    }

    public static RayTraceResult getMouseOverServer(LivingEntity lookingEntity, double range) {
        RayTraceResult result = raytraceEntityBlocks(lookingEntity, range);
        double rangeSq = range * range;
        Pair<Vector3d, Vector3d> startAndEnd = getStartAndEndLookVec(lookingEntity, (float) range);
        Vector3d eyePos = startAndEnd.getLeft();

        if (result.getType() != RayTraceResult.Type.MISS) {
            rangeSq = result.getLocation().distanceToSqr(eyePos);
        }

        double rangeSq2 = rangeSq;
        Vector3d hitVec = null;
        Entity focusedEntity = null;

        Vector3d lookVec = lookingEntity.getLookAngle().scale(range + 1);
        AxisAlignedBB box = lookingEntity.getBoundingBox().inflate(lookVec.x, lookVec.y, lookVec.z);

        for (Entity entity : lookingEntity.level.getEntities(lookingEntity, box, Entity::isPickable)) {
            AxisAlignedBB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vector3d> vec = aabb.clip(eyePos, startAndEnd.getRight());

            if (aabb.contains(eyePos)) {
                if (rangeSq2 >= 0.0D) {
                    focusedEntity = entity;
                    hitVec = vec.orElse(eyePos);
                    rangeSq2 = 0.0D;
                }
            } else if (vec.isPresent()) {
                double rangeSq3 = eyePos.distanceToSqr(vec.get());

                if (rangeSq3 < rangeSq2 || rangeSq2 == 0.0D) {
                    if (entity == entity.getVehicle() && !entity.canRiderInteract()) {
                        if (rangeSq2 == 0.0D) {
                            focusedEntity = entity;
                            hitVec = vec.get();
                        }
                    } else {
                        focusedEntity = entity;
                        hitVec = vec.get();
                        rangeSq2 = rangeSq3;
                    }
                }
            }
        }

        return focusedEntity != null && rangeSq2 < rangeSq ? new EntityRayTraceResult(focusedEntity, hitVec) : result;
    }

    private static RayTraceResult raytraceEntityBlocks(LivingEntity entity, double range) {
        Pair<Vector3d, Vector3d> startAndEnd = getStartAndEndLookVec(entity, (float) range);
        RayTraceContext ctx = new RayTraceContext(startAndEnd.getLeft(), startAndEnd.getRight(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity);
        return entity.level.clip(ctx);
    }
}
