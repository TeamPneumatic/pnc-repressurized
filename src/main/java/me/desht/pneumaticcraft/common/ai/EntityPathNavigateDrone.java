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

package me.desht.pneumaticcraft.common.ai;

import com.google.common.collect.Lists;
import me.desht.pneumaticcraft.api.drone.IPathNavigator;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticleTrail;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class EntityPathNavigateDrone extends FlyingPathNavigator implements IPathNavigator {
    private final EntityDrone droneEntity;
    private boolean forceTeleport;
    private int teleportCounter = -1;
    private BlockPos telPos;
    private static final int TELEPORT_TICKS = 120;
    private int stuckTicks = 0;

    public EntityPathNavigateDrone(EntityDrone droneEntity, World world) {
        super(droneEntity, world);
        this.droneEntity = droneEntity;
    }

    @Override
    public boolean moveTo(Entity p_75497_1_, double p_75497_2_) {
        return super.moveTo(p_75497_1_, p_75497_2_) || isGoingToTeleport();
    }

    /**
     * Returns the path to the given EntityLiving
     */
    @Override
    public Path createPath(Entity par1Entity, int p2) {
        BlockPos pos = new BlockPos(par1Entity.getX(), par1Entity.getBoundingBox().minY, par1Entity.getZ());

        if ((par1Entity instanceof ItemEntity && !droneEntity.isBlockValidPathfindBlock(pos)) || par1Entity instanceof AbstractMinecartEntity) {
            // items can end up with a blockpos of the ground they're sitting on,
            // which will prevent the drone pathfinding to them
            // minecarts apparently prevent the drone moving to the same blockpos
            if (droneEntity.isBlockValidPathfindBlock(pos.above())) {
                pos = pos.above();
            }
        }

        // p2 parameter shortens the path length. It appears vanilla uses it for villagers when they navigate to their work site.
        // Drones generally don't use it and just pass 0 to the super call, apart from a kludge related to "tall" blocks like walls; see below
        return createPath(pos, p2);
    }

    void setForceTeleport(boolean forceTeleport) {
        this.forceTeleport = forceTeleport;
    }

    @Nullable
    @Override
    public Path createPath(BlockPos pos, int p2) {
        // When the destination is not a valid block, we can stop right away
        if (!droneEntity.isBlockValidPathfindBlock(pos))
            return null;

        // 0.75 is the squared dist from a block corner to its center (0.5^2 + 0.5^2 + 0.5^2)
        if (droneEntity.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 0.75) {
            // TODO 1.14 what does this boolean do?  true or false here?  appears to be villager-related...
            return new Path(Lists.newArrayList(new PathPoint(pos.getX(), pos.getY(), pos.getZ())), pos, true);
        }

        // Store the potential teleport destination
        telPos = pos;

        // If we are forced to teleport, trigger right away
        if (forceTeleport) {
            teleportCounter = 0;
            return null;
        }

        droneEntity.setStandby(false);

        Path path;
        boolean tallBlockKludge = false;
        BlockPos below = pos.below();
        VoxelShape shape = level.getBlockState(below).getCollisionShape(level, below);
        if (!shape.isEmpty() && shape.bounds().maxY > 1) {
            // pathfinding to the space above a "tall" block and using distance == 0 doesn't work
            // walls and fences are the main culprit here (any others?)
            path = super.createPath(pos, 1);
            tallBlockKludge = true;
        } else {
            path = super.createPath(pos, 0);
        }

        // Only paths that actually end up where we want to are valid, not just partway
        // (but if we had to stop short due to a "tall" block, account for that)
        if (path != null) {
            PathPoint lastPoint = path.getEndNode();
            if (lastPoint != null && pos.distManhattan(lastPoint.asBlockPos()) > (tallBlockKludge ? 1 : 0)) {
                path = null;
            }
        }

        if (path == null && teleportationAllowed(pos)) {
            // No valid flight path: teleport instead, but don't reset the teleport counter if it's already in progress
            if (teleportCounter == -1) teleportCounter = 0;
        } else {
            // Valid path or teleportation disallowed: cancel any teleport in-progress
            teleportCounter = -1;
        }

        return path;
    }

    private boolean teleportationAllowed(BlockPos pos) {
        int max = PNCConfig.Common.Advanced.maxDroneTeleportRange;
        return !droneEntity.isTeleportRangeLimited() || max == 0 || pos.closerThan(droneEntity.getDronePos(), max);
    }

    @Override
    public boolean isGoingToTeleport() {
        return teleportCounter >= 0;
    }

    @Override
    public boolean isDone() {
        return super.isDone() && !isGoingToTeleport();
    }

    @Override
    public void tick() {
        ++tick;
        if (isGoingToTeleport()) {
            if (teleportCounter == 0 || teleportCounter == 60) {
                droneEntity.level.playSound(null, droneEntity.blockPosition(), ModSounds.HUD_INIT.get(), SoundCategory.NEUTRAL, 0.3f, teleportCounter == 0 ? 0.7F : 1F);
            }

            if (teleportCounter < TELEPORT_TICKS - 40) {
                Random rand = droneEntity.getRandom();
                float f = (rand.nextFloat() - 0.5F) * 0.02F * teleportCounter;
                float f1 = (rand.nextFloat() - 0.5F) * 0.02F * teleportCounter;
                float f2 = (rand.nextFloat() - 0.5F) * 0.02F * teleportCounter;
                NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.PORTAL, droneEntity.getX(), droneEntity.getY(), droneEntity.getZ(), f, f1, f2), droneEntity);
            }

            if (++teleportCounter > TELEPORT_TICKS) {
                if (droneEntity.isBlockValidPathfindBlock(telPos)) {
                    teleport();
                }
                teleportCounter = -1;
                moveTo((Path) null, 0);
                droneEntity.getMoveControl().setWantedPosition(telPos.getX(), telPos.getY(), telPos.getZ(), droneEntity.getDroneSpeed());
                droneEntity.addAirToDrone(-PneumaticValues.DRONE_USAGE_TELEPORT);
            }
        } else {
            if (!isDone()) {
                followThePath();
                if (path != null && !path.isDone()) {
                    if (PNCConfig.Common.Advanced.stuckDroneTeleportTicks > 0 && mob.getDeltaMovement().lengthSqr() < 0.0001) {
                        if (stuckTicks++ > PNCConfig.Common.Advanced.stuckDroneTeleportTicks) {
                            Vector3d v = droneEntity.getDronePos();
                            droneEntity.getDebugger().addEntry("pneumaticcraft.gui.progWidget.general.debug.stuckBlock",
                                    new BlockPos(Math.round(v.x), Math.round(v.y), Math.round(v.z)));
                            teleportCounter = 0;
                            telPos = path.getTarget();
                            stuckTicks = 0;
                        }
                    } else {
                        stuckTicks = 0;
                    }
                    if (!isDone()) {
                        Vector3d vec32 = path.getNextEntityPos(mob);
                        mob.getMoveControl().setWantedPosition(vec32.x, vec32.y, vec32.z, speedModifier);
                    }
                }
            }
        }
    }

    public void teleport() {
        Vector3d dest = Vector3d.atCenterOf(telPos);
        NetworkHandler.sendToAllTracking(new PacketSpawnParticleTrail(ParticleTypes.PORTAL,
                        droneEntity.getX(), droneEntity.getY(), droneEntity.getZ(),
                        dest.x, dest.y, dest.z),
                droneEntity);
        droneEntity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        droneEntity.setPos(dest.x, dest.y, dest.z);
    }

    @Override
    public boolean moveToXYZ(double x, double y, double z) {
        boolean success = moveTo(x, y, z, droneEntity.getDroneSpeed());
        if (success) forceRidingEntityPaths();
        return success;
    }

    @Override
    public boolean moveToEntity(Entity entity) {
        boolean success = moveTo(entity, droneEntity.getDroneSpeed());
        if (success) forceRidingEntityPaths();
        return success;
    }

    /**
     * Override to prevent MobEntity#updateEntityActionState() assigning a path with a higher speed.
     */
    @Override
    public boolean moveTo(Path pathentityIn, double speedIn) {
        return super.moveTo(pathentityIn, droneEntity.getDroneSpeed());
    }

    /**
     * Hack to prevent riding entities to override the Drone's path (instead they will assign the Drone's path)
     */
    private void forceRidingEntityPaths() {
        for (Entity ridingEntity : droneEntity.getPassengers()) {
            if (ridingEntity instanceof MobEntity) {
                MobEntity ridingLiving = (MobEntity) ridingEntity;
                ridingLiving.getNavigation().moveTo(droneEntity.getNavigation().getPath(), droneEntity.getDroneSpeed());
            }
        }
    }

    @Override
    public boolean hasNoPath() {
        return isDone();
    }

    @Override
    public boolean canMoveDirectly(Vector3d p_75493_1_, Vector3d p_75493_2_, int p_75493_3_, int p_75493_4_, int p_75493_5_) {
        return false;
    }

    @Override
    protected PathFinder createPathFinder(int r) {
        this.nodeEvaluator = new NodeProcessorDrone();
        // no longer a need for PathFinderDrone subclass, since vanilla PathFinder takes a max distance param.
        return new PathFinder(this.nodeEvaluator, 1000);
    }

    @Override
    protected Vector3d getTempMobPos() {
        return droneEntity.getDronePos();
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }
}
