package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class NodeProcessorDrone extends FlyingNodeProcessor {
    @Nullable
    @Override
    protected PathPoint getNode(int x, int y, int z) {
        return ((EntityDrone) mob).isBlockValidPathfindBlock(new BlockPos(x, y, z)) ? super.getNode(x, y, z) : null;
    }

    /**
     * Override this, because the super method adds diagonals, this is fancy but doesn't work well with drones (drones stuck behind a wall).
     */
    @Override
    public int getNeighbors(PathPoint[] pathOptions, PathPoint currentPoint) {
        return super.getNeighbors(pathOptions, currentPoint);
        // findPathOptions

//        int i = 0;
//
//        for(Direction dir : DirectionUtil.VALUES){
//            PathPoint point = getNode(currentPoint.x + dir.getStepX(), currentPoint.y + dir.getStepY(), currentPoint.z + dir.getStepZ());
//            if(point != null && !point.closed) {
//                pathOptions[i++] = point;
//            }
//        }
//
//        return i;
    }
}
