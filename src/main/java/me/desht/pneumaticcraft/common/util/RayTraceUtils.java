package me.desht.pneumaticcraft.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
        return entity.world.rayTraceBlocks(ctx);
    }

    public static Pair<Vector3d, Vector3d> getStartAndEndLookVec(LivingEntity entity, double maxDistance) {
        Vector3d entityVec;
        if (entity.world.isRemote && entity instanceof PlayerEntity) {
            entityVec = new Vector3d(entity.getPosX(), entity.getPosY() + 1.6200000000000001D, entity.getPosZ());
        } else {
            entityVec = new Vector3d(entity.getPosX(), entity.getPosY() + entity.getEyeHeight() - (entity.isSneaking() ? 0.08 : 0), entity.getPosZ());
        }
        Vector3d entityLookVec = entity.getLook(1.0F);
        Vector3d maxDistVec = entityVec.add(entityLookVec.scale(maxDistance));
        return new ImmutablePair<>(entityVec, maxDistVec);
    }

    public static RayTraceResult getMouseOverServer(LivingEntity lookingEntity, double range) {
        RayTraceResult result = raytraceEntityBlocks(lookingEntity, range);
        double rangeSq = range * range;
        Pair<Vector3d, Vector3d> startAndEnd = getStartAndEndLookVec(lookingEntity, (float) range);
        Vector3d eyePos = startAndEnd.getLeft();

        if (result.getType() != RayTraceResult.Type.MISS) {
            rangeSq = result.getHitVec().squareDistanceTo(eyePos);
        }

        double rangeSq2 = rangeSq;
        Vector3d hitVec = null;
        Entity focusedEntity = null;

        Vector3d lookVec = lookingEntity.getLookVec().scale(range + 1);
        AxisAlignedBB box = lookingEntity.getBoundingBox().grow(lookVec.x, lookVec.y, lookVec.z);

        for (Entity entity : lookingEntity.world.getEntitiesInAABBexcluding(lookingEntity, box, Entity::canBeCollidedWith)) {
            AxisAlignedBB aabb = entity.getBoundingBox().grow(entity.getCollisionBorderSize());
            Optional<Vector3d> vec = aabb.rayTrace(eyePos, startAndEnd.getRight());

            if (aabb.contains(eyePos)) {
                if (rangeSq2 >= 0.0D) {
                    focusedEntity = entity;
                    hitVec = vec.orElse(eyePos);
                    rangeSq2 = 0.0D;
                }
            } else if (vec.isPresent()) {
                double rangeSq3 = eyePos.squareDistanceTo(vec.get());

                if (rangeSq3 < rangeSq2 || rangeSq2 == 0.0D) {
                    if (entity == entity.getRidingEntity() && !entity.canRiderInteract()) {
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
        return entity.world.rayTraceBlocks(ctx);
    }
}
