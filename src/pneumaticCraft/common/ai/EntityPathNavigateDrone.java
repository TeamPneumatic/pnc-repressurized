package pneumaticCraft.common.ai;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinderDrone;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import pneumaticCraft.api.drone.IPathNavigator;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketPlaySound;
import pneumaticCraft.common.network.PacketSpawnParticle;
import pneumaticCraft.lib.Sounds;

public class EntityPathNavigateDrone extends PathNavigate implements IPathNavigator{

    private final EntityDrone pathfindingEntity;
    public boolean pathThroughLiquid;
    private boolean forceTeleport;
    private int teleportCounter = -1;
    private int telX, telY, telZ;
    private static final int TELEPORT_TICKS = 120;

    public EntityPathNavigateDrone(EntityDrone pathfindingEntity, World par2World){
        super(pathfindingEntity, par2World);
        this.pathfindingEntity = pathfindingEntity;
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity p_75497_1_, double p_75497_2_){
        return super.tryMoveToEntityLiving(p_75497_1_, p_75497_2_) || isGoingToTeleport();
    }

    /**
     * Returns the path to the given coordinates
     */
    @Override
    public PathEntity getPathToXYZ(double par1, double par3, double par5){
        return getEntityPathToXYZ(pathfindingEntity, MathHelper.floor_double(par1), (int)par3, MathHelper.floor_double(par5), getPathSearchRange(), false, false, true, false);
    }

    /**
     * Returns the path to the given EntityLiving
     */
    @Override
    public PathEntity getPathToEntityLiving(Entity par1Entity){
        return getPathEntityToEntity(pathfindingEntity, par1Entity, getPathSearchRange(), false, false, true, false);
    }

    public void setForceTeleport(boolean forceTeleport){
        this.forceTeleport = forceTeleport;
    }

    private PathEntity getPathEntityToEntity(EntityDrone par1Entity, Entity par2Entity, float par3, boolean par4, boolean par5, boolean par6, boolean par7){
        /* int i = MathHelper.floor_double(par1Entity.posX);
         int j = MathHelper.floor_double(par1Entity.posY + 1.0D);
         int k = MathHelper.floor_double(par1Entity.posZ);
         int l = (int)(par3 + 16.0F);
         int i1 = i - l;
         int j1 = j - l;
         int k1 = k - l;
         int l1 = i + l;
         int i2 = j + l;
         int j2 = k + l;
         ChunkCache chunkcache = new ChunkCache(par1Entity.worldObj, i1, j1, k1, l1, i2, j2, 0);
         PathEntity pathentity = new PathFinderDrone(par1Entity, chunkcache, par4, par5, pathThroughLiquid, par7).createEntityPathTo(par1Entity, par2Entity, par3);
         return pathentity;*/
        return getEntityPathToXYZ(par1Entity, (int)Math.floor(par2Entity.posX), (int)Math.floor(par2Entity.posY), (int)Math.floor(par2Entity.posZ), par3, par4, par5, par6, par7);
    }

    public PathEntity getEntityPathToXYZ(EntityDrone par1Entity, int par2, int par3, int par4, float par5, boolean par6, boolean par7, boolean par8, boolean par9){
        if(!par1Entity.isBlockValidPathfindBlock(par2, par3, par4)) return null;
        PathEntity pathentity = null;
        int l = MathHelper.floor_double(par1Entity.posX);
        int i1 = MathHelper.floor_double(par1Entity.posY);
        int j1 = MathHelper.floor_double(par1Entity.posZ);
        if(!forceTeleport || l == par2 && i1 == par3 && j1 == par4) {
            int k1 = (int)(par5 + 8.0F);
            int l1 = l - k1;
            int i2 = i1 - k1;
            int j2 = j1 - k1;
            int k2 = l + k1;
            int l2 = i1 + k1;
            int i3 = j1 + k1;
            ChunkCache chunkcache = new ChunkCache(par1Entity.worldObj, l1, i2, j2, k2, l2, i3, 0);
            pathentity = new PathFinderDrone(par1Entity, chunkcache, par6, par7, pathThroughLiquid, par9).createEntityPathTo(par1Entity, par2, par3, par4, par5);
            if(pathentity != null) {
                PathPoint finalPoint = pathentity.getFinalPathPoint();
                if(finalPoint == null || finalPoint.xCoord != par2 || finalPoint.yCoord != par3 || finalPoint.zCoord != par4) pathentity = null;
            }
        }
        teleportCounter = pathentity != null ? -1 : 0;
        telX = par2;
        telY = par3;
        telZ = par4;
        par1Entity.setStandby(false);
        return pathentity;
    }

    @Override
    public float getPathSearchRange(){
        return (float)pathfindingEntity.getRange();
    }

    @Override
    public boolean isGoingToTeleport(){
        return teleportCounter >= 0;
    }

    @Override
    public boolean noPath(){
        return super.noPath() && !isGoingToTeleport();
    }

    @Override
    public void onUpdateNavigation(){
        if(isGoingToTeleport()) {
            if(teleportCounter == 0 || teleportCounter == 60) {
                NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.HUD_INIT, pathfindingEntity.posX, pathfindingEntity.posY, pathfindingEntity.posZ, 0.1F, teleportCounter == 0 ? 0.7F : 1F, true), pathfindingEntity.worldObj);
            }

            if(teleportCounter < TELEPORT_TICKS - 40) {
                Random rand = pathfindingEntity.getRNG();
                float f = (rand.nextFloat() - 0.5F) * 0.02F * teleportCounter;
                float f1 = (rand.nextFloat() - 0.5F) * 0.02F * teleportCounter;
                float f2 = (rand.nextFloat() - 0.5F) * 0.02F * teleportCounter;
                NetworkHandler.sendToAllAround(new PacketSpawnParticle("portal", pathfindingEntity.posX, pathfindingEntity.posY, pathfindingEntity.posZ, f, f1, f2), pathfindingEntity.worldObj);
            }

            if(++teleportCounter > TELEPORT_TICKS) {
                if(pathfindingEntity.isBlockValidPathfindBlock(telX, telY, telZ)) {
                    teleport();
                }
                teleportCounter = -1;
                setPath(null, 0);
                pathfindingEntity.getMoveHelper().setMoveTo(telX, telY, telZ, pathfindingEntity.getSpeed());
                pathfindingEntity.addAir(null, -10000);
            }
        } else {
            super.onUpdateNavigation();
        }
    }

    public void teleport(){

        Random rand = pathfindingEntity.getRNG();
        double width = pathfindingEntity.width;
        double height = pathfindingEntity.height;

        short short1 = 128;

        for(int l = 0; l < short1; ++l) {
            double d6 = l / (short1 - 1.0D);
            float f = (rand.nextFloat() - 0.5F) * 0.2F;
            float f1 = (rand.nextFloat() - 0.5F) * 0.2F;
            float f2 = (rand.nextFloat() - 0.5F) * 0.2F;
            double d7 = pathfindingEntity.posX + (telX + 0.5 - pathfindingEntity.posX) * d6 + (rand.nextDouble() - 0.5D) * width * 2.0D;
            double d8 = pathfindingEntity.posY + (telY - pathfindingEntity.posY) * d6 + rand.nextDouble() * height;
            double d9 = pathfindingEntity.posZ + (telZ + 0.5 - pathfindingEntity.posZ) * d6 + (rand.nextDouble() - 0.5D) * width * 2.0D;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle("portal", d7, d8, d9, f, f1, f2), pathfindingEntity.worldObj);
        }

        pathfindingEntity.worldObj.playSoundEffect(pathfindingEntity.posX, pathfindingEntity.posY, pathfindingEntity.posZ, "mob.endermen.portal", 1.0F, 1.0F);
        pathfindingEntity.playSound("mob.endermen.portal", 1.0F, 1.0F);

        pathfindingEntity.setPosition(telX + 0.5, telY + 0.5, telZ + 0.5);
    }

    @Override
    public boolean moveToXYZ(double x, double y, double z){
        return tryMoveToXYZ(x, y, z, pathfindingEntity.getSpeed());
    }

    @Override
    public boolean moveToEntity(Entity entity){
        return tryMoveToEntityLiving(entity, pathfindingEntity.getSpeed());
    }

    @Override
    public boolean hasNoPath(){
        return noPath();
    }

    @Override
    public boolean isDirectPathBetweenPoints(Vec3 p_75493_1_, Vec3 p_75493_2_, int p_75493_3_, int p_75493_4_, int p_75493_5_){
        return false;
    }
}
