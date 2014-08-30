package pneumaticCraft.common.ai;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinderDrone;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import pneumaticCraft.common.entity.living.EntityDrone;

public class EntityPathNavigateDrone extends PathNavigate{

    private final EntityDrone pathfindingEntity;
    public boolean pathThroughLiquid;

    public EntityPathNavigateDrone(EntityDrone pathfindingEntity, World par2World){
        super(pathfindingEntity, par2World);
        this.pathfindingEntity = pathfindingEntity;
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

    private PathEntity getEntityPathToXYZ(EntityDrone par1Entity, int par2, int par3, int par4, float par5, boolean par6, boolean par7, boolean par8, boolean par9){
        int l = MathHelper.floor_double(par1Entity.posX);
        int i1 = MathHelper.floor_double(par1Entity.posY);
        int j1 = MathHelper.floor_double(par1Entity.posZ);
        int k1 = (int)(par5 + 8.0F);
        int l1 = l - k1;
        int i2 = i1 - k1;
        int j2 = j1 - k1;
        int k2 = l + k1;
        int l2 = i1 + k1;
        int i3 = j1 + k1;
        ChunkCache chunkcache = new ChunkCache(par1Entity.worldObj, l1, i2, j2, k2, l2, i3, 0);
        PathEntity pathentity = new PathFinderDrone(par1Entity, chunkcache, par6, par7, pathThroughLiquid, par9).createEntityPathTo(par1Entity, par2, par3, par4, par5);
        if(pathentity != null) {
            PathPoint finalPoint = pathentity.getFinalPathPoint();
            if(finalPoint == null || finalPoint.xCoord != par2 || finalPoint.yCoord != par3 || finalPoint.zCoord != par4) pathentity = null;
        }
        return pathentity;
    }

    @Override
    public float getPathSearchRange(){
        return (float)pathfindingEntity.getRange();
    }
}
