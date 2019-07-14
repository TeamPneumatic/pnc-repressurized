package me.desht.pneumaticcraft.common.ai;

import com.google.common.collect.Lists;
import me.desht.pneumaticcraft.api.drone.IPathNavigator;
import me.desht.pneumaticcraft.common.core.Sounds;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class EntityPathNavigateDrone extends FlyingPathNavigator implements IPathNavigator {

    private final EntityDrone pathfindingEntity;
    public boolean pathThroughLiquid;
    private boolean forceTeleport;
    private int teleportCounter = -1;
    private BlockPos telPos;
    private static final int TELEPORT_TICKS = 120;

    public EntityPathNavigateDrone(EntityDrone pathfindingEntity, World par2World) {
        super(pathfindingEntity, par2World);
        this.pathfindingEntity = pathfindingEntity;
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity p_75497_1_, double p_75497_2_) {
        return super.tryMoveToEntityLiving(p_75497_1_, p_75497_2_) || isGoingToTeleport();
    }

    /**
     * Returns the path to the given EntityLiving
     */
    @Override
    public Path getPathToEntityLiving(Entity par1Entity) {
        BlockPos pos = new BlockPos(par1Entity.posX, par1Entity.getBoundingBox().minY, par1Entity.posZ);

        if ((par1Entity instanceof ItemEntity && !pathfindingEntity.isBlockValidPathfindBlock(pos)) || par1Entity instanceof AbstractMinecartEntity) {
            // items can end up with a blockpos of the ground they're sitting on,
            // which will prevent the drone pathfinding to them
            // minecarts apparently prevent the drone moving to the same blockpos
            if (pathfindingEntity.isBlockValidPathfindBlock(pos.up())) {
                pos = pos.up();
            }
        }
        return getPathToPos(pos);
    }

    void setForceTeleport(boolean forceTeleport) {
        this.forceTeleport = forceTeleport;
    }

    @Nullable
    @Override
    public Path getPathToPos(BlockPos pos) {
        // When the destination is not a valid block, we can stop right away
        if (!pathfindingEntity.isBlockValidPathfindBlock(pos))
            return null;

        // 0.75 is the squared dist from a block corner to its center (0.5^2 + 0.5^2 + 0.5^2)
        if(pathfindingEntity.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 0.75) {
            return new Path(Lists.newArrayList(new PathPoint(pos.getX(), pos.getY(), pos.getZ())));
        }

        //Store the potential teleport destination
        telPos = pos;        
        
        //If we are forced to teleport, trigger right away
        if (forceTeleport) {
            teleportCounter = 0;
            return null;
        }
        
        pathfindingEntity.setStandby(false);
        Path path = super.getPathToPos(pos);
        
        // Only paths that actually end up where we want to are valid, not just halfway.
        if(path != null){
            PathPoint lastPoint = path.getFinalPathPoint();
            if(lastPoint != null && (lastPoint.x != pos.getX() || lastPoint.y != pos.getY() || lastPoint.z != pos.getZ())){
                path = null;
            }
        }
        
        if (path == null) {
            // No valid flight path: teleport instead, but don't reset the teleport counter if it's already in progress
            if (teleportCounter == -1) teleportCounter = 0;
        } else {
            // Valid path: cancel any teleport in-progress
            teleportCounter = -1;
        }

        return path;
    }

    @Override
    public float getPathSearchRange() {
        return (float) pathfindingEntity.getRange();
    }

    @Override
    public boolean isGoingToTeleport() {
        return teleportCounter >= 0;
    }

    @Override
    public boolean noPath() {
        return super.noPath() && !isGoingToTeleport();
    }

    @Override
    public void tick() {
        if (isGoingToTeleport()) {
            if (teleportCounter == 0 || teleportCounter == 60) {
                NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.HUD_INIT, SoundCategory.PLAYERS, pathfindingEntity.posX, pathfindingEntity.posY, pathfindingEntity.posZ, 0.1F, teleportCounter == 0 ? 0.7F : 1F, true), pathfindingEntity.world);
            }

            if (teleportCounter < TELEPORT_TICKS - 40) {
                Random rand = pathfindingEntity.getRNG();
                float f = (rand.nextFloat() - 0.5F) * 0.02F * teleportCounter;
                float f1 = (rand.nextFloat() - 0.5F) * 0.02F * teleportCounter;
                float f2 = (rand.nextFloat() - 0.5F) * 0.02F * teleportCounter;
                NetworkHandler.sendToAllAround(new PacketSpawnParticle(ParticleTypes.PORTAL, pathfindingEntity.posX, pathfindingEntity.posY, pathfindingEntity.posZ, f, f1, f2), pathfindingEntity.world);
            }

            if (++teleportCounter > TELEPORT_TICKS) {
                if (pathfindingEntity.isBlockValidPathfindBlock(telPos)) {
                    teleport();
                }
                teleportCounter = -1;
                setPath(null, 0);
                pathfindingEntity.getMoveHelper().setMoveTo(telPos.getX(), telPos.getY(), telPos.getZ(), pathfindingEntity.getSpeed());
                pathfindingEntity.addAir(null, -10000);
            }
        } else {
            // super.onUpdateNavigation();
            if (!noPath()) {
                pathFollow();

                if (!noPath()) {
                    Vec3d vec32 = currentPath.getPosition(entity);

                    if (vec32 != null) {
                        entity.getMoveHelper().setMoveTo(vec32.x, vec32.y, vec32.z, speed);
                    }
                }
            }
        }
    }

    public void teleport() {
        Random rand = pathfindingEntity.getRNG();
        double width = pathfindingEntity.getWidth();
        double height = pathfindingEntity.getHeight();

        short short1 = 128;

        for (int l = 0; l < short1; ++l) {
            double d6 = l / (short1 - 1.0D);
            float f = (rand.nextFloat() - 0.5F) * 0.2F;
            float f1 = (rand.nextFloat() - 0.5F) * 0.2F;
            float f2 = (rand.nextFloat() - 0.5F) * 0.2F;
            double d7 = pathfindingEntity.posX + (telPos.getX() + 0.5 - pathfindingEntity.posX) * d6 + (rand.nextDouble() - 0.5D) * width * 2.0D;
            double d8 = pathfindingEntity.posY + (telPos.getY() - pathfindingEntity.posY) * d6 + rand.nextDouble() * height;
            double d9 = pathfindingEntity.posZ + (telPos.getZ() + 0.5 - pathfindingEntity.posZ) * d6 + (rand.nextDouble() - 0.5D) * width * 2.0D;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(ParticleTypes.PORTAL, d7, d8, d9, f, f1, f2), pathfindingEntity.world);
        }

        pathfindingEntity.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
        pathfindingEntity.setPosition(telPos.getX() + 0.5, telPos.getY() + 0.5, telPos.getZ() + 0.5);
    }

    @Override
    public boolean moveToXYZ(double x, double y, double z) {
        boolean success = tryMoveToXYZ(x, y, z, pathfindingEntity.getSpeed());
        if(success) forceRidingEntityPaths();
        return success;
    }

    @Override
    public boolean moveToEntity(Entity entity) {
        boolean success = tryMoveToEntityLiving(entity, pathfindingEntity.getSpeed());
        if(success) forceRidingEntityPaths();
        return success;
    }
    
    /**
     * Override to prevent MobEntity#updateEntityActionState() assigning a path with a higher speed.
     */
    @Override
    public boolean setPath(Path pathentityIn, double speedIn){
        return super.setPath(pathentityIn, pathfindingEntity.getSpeed());
    }
    
    /**
     * Hack to prevent riding entities to override the Drone's path (instead they will assign the Drone's path)
     */
    private void forceRidingEntityPaths(){
        for(Entity ridingEntity : pathfindingEntity.getPassengers()){
            if(ridingEntity instanceof MobEntity){
                MobEntity ridingLiving = (MobEntity)ridingEntity;
                ridingLiving.getNavigator().setPath(pathfindingEntity.getNavigator().getPath(), pathfindingEntity.getSpeed());
            }
        }
    }

    @Override
    public boolean hasNoPath() {
        return noPath();
    }

    @Override
    public boolean isDirectPathBetweenPoints(Vec3d p_75493_1_, Vec3d p_75493_2_, int p_75493_3_, int p_75493_4_, int p_75493_5_) {
        return false;
    }

    @Override
    protected PathFinder getPathFinder(int r) {
        this.nodeProcessor = new NodeProcessorDrone();
        // no longer a need for PathFinderDrone subclass, since vanilla PathFinder takes a max distance param.
        return new PathFinder(this.nodeProcessor, Integer.MAX_VALUE);
    }

    @Override
    protected Vec3d getEntityPosition() {
        return pathfindingEntity.getDronePos();
    }

    @Override
    protected boolean canNavigate() {
        return true;
    }
}
