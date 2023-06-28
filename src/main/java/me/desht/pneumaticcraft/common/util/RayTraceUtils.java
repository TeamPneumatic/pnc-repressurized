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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public class RayTraceUtils {
    public static HitResult getEntityLookedObject(LivingEntity entity, double maxDistance) {
        Pair<Vec3, Vec3> vecs = getStartAndEndLookVec(entity, maxDistance);
        ClipContext ctx = new ClipContext(vecs.getLeft(), vecs.getRight(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        return entity.level().clip(ctx);
    }

    public static Pair<Vec3, Vec3> getStartAndEndLookVec(LivingEntity entity, double maxDistance) {
        Vec3 entityVec = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        Vec3 maxDistVec = entityVec.add(entity.getViewVector(1F).scale(maxDistance));
        return new ImmutablePair<>(entity.getEyePosition(1F), maxDistVec);
    }

    public static HitResult getMouseOverServer(LivingEntity lookingEntity, double range) {
        HitResult result = raytraceEntityBlocks(lookingEntity, range);
        double rangeSq = range * range;
        Pair<Vec3, Vec3> startAndEnd = getStartAndEndLookVec(lookingEntity, (float) range);
        Vec3 eyePos = startAndEnd.getLeft();

        if (result.getType() != HitResult.Type.MISS) {
            rangeSq = result.getLocation().distanceToSqr(eyePos);
        }

        double rangeSq2 = rangeSq;
        Vec3 hitVec = null;
        Entity focusedEntity = null;

        Vec3 lookVec = lookingEntity.getLookAngle().scale(range + 1);
        AABB box = lookingEntity.getBoundingBox().inflate(lookVec.x, lookVec.y, lookVec.z);

        for (Entity entity : lookingEntity.level().getEntities(lookingEntity, box, Entity::isPickable)) {
            AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> vec = aabb.clip(eyePos, startAndEnd.getRight());

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

        return focusedEntity != null && rangeSq2 < rangeSq ? new EntityHitResult(focusedEntity, hitVec) : result;
    }

    private static HitResult raytraceEntityBlocks(LivingEntity entity, double range) {
        Pair<Vec3, Vec3> startAndEnd = getStartAndEndLookVec(entity, (float) range);
        ClipContext ctx = new ClipContext(startAndEnd.getLeft(), startAndEnd.getRight(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        return entity.level().clip(ctx);
    }
}
