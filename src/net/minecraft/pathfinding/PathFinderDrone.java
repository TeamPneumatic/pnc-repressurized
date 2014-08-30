package net.minecraft.pathfinding;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.entity.living.EntityDrone;

/**
 * This class is highly derived from Minecraft's PathFinder class. As that class contains mostly I didn't see a way to making this class a subclass
 * of PathFinder. And yes, there are accesstransformers, but I think copying a class is better than editing a base class. Let me know if you know
 * better ways to do this.
 */

public class PathFinderDrone{

    private final EntityDrone drone;
    /** Used to find obstacles */
    private final IBlockAccess worldMap;

    /** The path being generated */
    private final Path path = new Path();

    /** The points in the path */
    private final IntHashMap pointMap = new IntHashMap();

    /** Selection of path points to add to the path */
    private final PathPoint[] pathOptions = new PathPoint[32];

    /** should the PathFinder go through wodden door blocks */
    // private final boolean isWoddenDoorAllowed;

    /**
     * should the PathFinder disregard BlockMovement type materials in its path
     */
    // private final boolean isMovementBlockAllowed;
    private boolean isPathingInWater;

    /** tells the FathFinder to not stop pathing underwater */
    private final boolean canEntityDrown;

    public PathFinderDrone(EntityDrone drone, IBlockAccess world, boolean pathFindThroughWoodenDoor,
            boolean movementBlockAllowed, boolean pathFindThroughWater, boolean canDrown){
        this.drone = drone;
        worldMap = world;
        //isWoddenDoorAllowed = pathFindThroughWoodenDoor;
        //    isMovementBlockAllowed = movementBlockAllowed;
        isPathingInWater = pathFindThroughWater;
        canEntityDrown = canDrown;
    }

    /**
     * Creates a path from one entity to another within a minimum distance
     */
    public PathEntity createEntityPathTo(Entity par1Entity, Entity par2Entity, float par3){
        return this.createEntityPathTo(par1Entity, par2Entity.posX, par2Entity.boundingBox.minY, par2Entity.posZ, par3);
    }

    /**
     * Creates a path from an entity to a specified location within a minimum distance
     */
    public PathEntity createEntityPathTo(Entity par1Entity, int par2, int par3, int par4, float par5){
        return this.createEntityPathTo(par1Entity, par2 + 0.5F, par3 + 0.5F, par4 + 0.5F, par5);
    }

    /**
     * Internal implementation of creating a path from an entity to a point
     */
    private PathEntity createEntityPathTo(Entity par1Entity, double par2, double par4, double par6, float par8){
        path.clearPath();
        pointMap.clearMap();
        boolean flag = isPathingInWater;
        int i = MathHelper.floor_double(par1Entity.boundingBox.minY + 0.5D);

        if(canEntityDrown && par1Entity.isInWater()) {
            i = (int)par1Entity.boundingBox.minY;

            for(Block j = worldMap.getBlock(MathHelper.floor_double(par1Entity.posX), i, MathHelper.floor_double(par1Entity.posZ)); j == Blocks.water || j == Blocks.flowing_water; j = worldMap.getBlock(MathHelper.floor_double(par1Entity.posX), i, MathHelper.floor_double(par1Entity.posZ))) {
                ++i;
            }

            flag = isPathingInWater;
            isPathingInWater = false;
        } else {
            i = MathHelper.floor_double(par1Entity.boundingBox.minY + 0.5D);
        }

        PathPoint pathpoint = openPoint(MathHelper.floor_double(par1Entity.boundingBox.minX), i, MathHelper.floor_double(par1Entity.boundingBox.minZ));
        PathPoint pathpoint1 = openPoint(MathHelper.floor_double(par2 - par1Entity.width / 2.0F), MathHelper.floor_double(par4), MathHelper.floor_double(par6 - par1Entity.width / 2.0F));
        PathPoint pathpoint2 = new PathPoint(MathHelper.floor_float(par1Entity.width + 1.0F), MathHelper.floor_float(par1Entity.height + 1.0F), MathHelper.floor_float(par1Entity.width + 1.0F));
        PathEntity pathentity = addToPath(par1Entity, pathpoint, pathpoint1, pathpoint2, par8);
        isPathingInWater = flag;
        return pathentity;
    }

    /**
     * Adds a path from start to end and returns the whole path (args: unused, start, end, unused, maxDistance)
     */
    private PathEntity addToPath(Entity par1Entity, PathPoint par2PathPoint, PathPoint par3PathPoint, PathPoint par4PathPoint, float par5){
        par2PathPoint.totalPathDistance = 0.0F;
        par2PathPoint.distanceToNext = par2PathPoint.distanceTo(par3PathPoint);
        par2PathPoint.distanceToTarget = par2PathPoint.distanceToNext;
        path.clearPath();
        path.addPoint(par2PathPoint);
        PathPoint pathpoint3 = par2PathPoint;

        while(!path.isPathEmpty()) {
            PathPoint pathpoint4 = path.dequeue();

            if(pathpoint4.equals(par3PathPoint)) {
                return createEntityPath(par2PathPoint, par3PathPoint);
            }

            if(pathpoint4.distanceTo(par3PathPoint) < pathpoint3.distanceTo(par3PathPoint)) {
                pathpoint3 = pathpoint4;
            }

            pathpoint4.isFirst = true;
            int i = findPathOptions(par1Entity, pathpoint4, par4PathPoint, par3PathPoint, par5);

            for(int j = 0; j < i; ++j) {
                PathPoint pathpoint5 = pathOptions[j];
                float f1 = pathpoint4.totalPathDistance + pathpoint4.distanceTo(pathpoint5);

                if(!pathpoint5.isAssigned() || f1 < pathpoint5.totalPathDistance) {
                    pathpoint5.previous = pathpoint4;
                    pathpoint5.totalPathDistance = f1;
                    pathpoint5.distanceToNext = pathpoint5.distanceTo(par3PathPoint);

                    if(pathpoint5.isAssigned()) {
                        path.changeDistance(pathpoint5, pathpoint5.totalPathDistance + pathpoint5.distanceToNext);
                    } else {
                        pathpoint5.distanceToTarget = pathpoint5.totalPathDistance + pathpoint5.distanceToNext;
                        path.addPoint(pathpoint5);
                    }
                }
            }
        }

        if(pathpoint3 == par2PathPoint) {
            return null;
        } else {
            return createEntityPath(par2PathPoint, pathpoint3);
        }
    }

    /**
     * populates pathOptions with available points and returns the number of options found (args: unused1, currentPoint,
     * unused2, targetPoint, maxDistance)
     */
    private int findPathOptions(Entity par1Entity, PathPoint par2PathPoint, PathPoint par3PathPoint, PathPoint par4PathPoint, float par5){
        int i = 0;
        byte b0 = 0;

        if(getVerticalOffset(par1Entity, par2PathPoint.xCoord, par2PathPoint.yCoord + 1, par2PathPoint.zCoord, par3PathPoint) == 1) {
            b0 = 1;
        }

        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            PathPoint safePoint = getSafePoint(par1Entity, par2PathPoint.xCoord + dir.offsetX, par2PathPoint.yCoord + dir.offsetY, par2PathPoint.zCoord + dir.offsetZ, par3PathPoint, b0);
            if(safePoint != null && !safePoint.isFirst && safePoint.distanceTo(par4PathPoint) < par5) {
                pathOptions[i++] = safePoint;
            }
        }

        return i;
    }

    /**
     * Returns a point that the entity can safely move to
     */
    private PathPoint getSafePoint(Entity par1Entity, int par2, int par3, int par4, PathPoint par5PathPoint, int par6){
        PathPoint pathpoint1 = null;
        /*  int i1 = getVerticalOffset(par1Entity, par2, par3, par4, par5PathPoint);

          if(i1 == 2) {
              return openPoint(par2, par3, par4);
          } else {
              if(i1 == 1) {
                  pathpoint1 = openPoint(par2, par3, par4);
              }

              if(pathpoint1 == null && par6 > 0 && i1 != -3 && i1 != -4 && getVerticalOffset(par1Entity, par2, par3 + par6, par4, par5PathPoint) == 1) {
                  pathpoint1 = openPoint(par2, par3 + par6, par4);
                  par3 += par6;
              }

              if(pathpoint1 != null) {
                  int j1 = 0;
                  int k1 = 0;

                  while(par3 > 0) {
                      k1 = getVerticalOffset(par1Entity, par2, par3 - 1, par4, par5PathPoint);

                      if(isPathingInWater && k1 == -1) {
                          return null;
                      }

                      if(k1 != 1) {
                          break;
                      }

                       if(j1++ >= par1Entity.getMaxSafePointTries()) {
                         //  return null;
                       }

                      --par3;

                      if(par3 > 0) {
                          pathpoint1 = openPoint(par2, par3, par4);
                      }
                  }

                  if(k1 == -2) {
                      return null;
                  }
              }*/
        if(drone.isBlockValidPathfindBlock(par2, par3, par4)) pathpoint1 = openPoint(par2, par3, par4);

        return pathpoint1;
    }

    /**
     * Returns a mapped point or creates and adds one
     */
    private final PathPoint openPoint(int par1, int par2, int par3){
        int l = PathPoint.makeHash(par1, par2, par3);
        PathPoint pathpoint = (PathPoint)pointMap.lookup(l);

        if(pathpoint == null) {
            pathpoint = new PathPoint(par1, par2, par3);
            pointMap.addKey(l, pathpoint);
        }

        return pathpoint;
    }

    /**
     * Checks if an entity collides with blocks at a position. Returns 1 if clear, 0 for colliding with any solid block,
     * -1 for water(if avoiding water) but otherwise clear, -2 for lava, -3 for fence, -4 for closed trapdoor, 2 if
     * otherwise clear except for open trapdoor or water(if not avoiding)
     */
    public int getVerticalOffset(Entity par1Entity, int par2, int par3, int par4, PathPoint par5PathPoint){
        return 1;
        // return func_82565_a(par1Entity, par2, par3, par4, par5PathPoint, isPathingInWater, isMovementBlockAllowed, isWoddenDoorAllowed);
    }

    /**
     * Returns a new PathEntity for a given start and end point
     */
    private PathEntity createEntityPath(PathPoint par1PathPoint, PathPoint par2PathPoint){
        int i = 1;
        PathPoint pathpoint2;

        for(pathpoint2 = par2PathPoint; pathpoint2.previous != null; pathpoint2 = pathpoint2.previous) {
            ++i;
        }

        PathPoint[] apathpoint = new PathPoint[i];
        pathpoint2 = par2PathPoint;
        --i;

        for(apathpoint[i] = par2PathPoint; pathpoint2.previous != null; apathpoint[i] = pathpoint2) {
            pathpoint2 = pathpoint2.previous;
            --i;
        }

        return new PathEntity(apathpoint);
    }

}
