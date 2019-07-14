package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class NodeProcessorDrone extends FlyingNodeProcessor {
    @Nullable
    @Override
    protected PathPoint openPoint(int x, int y, int z) {
        return ((EntityDrone) entity).isBlockValidPathfindBlock(new BlockPos(x, y, z)) ? super.openPoint(x, y, z) : null;
    }

    /**
     * Override this, because the super method adds diagonals, this is fancy but doesn't work well with drones (drones stuck behind a wall).
     */
    @Override
    public int func_222859_a(PathPoint[] pathOptions, PathPoint currentPoint) {
        // findPathOptions

        int i = 0;

        for(Direction dir : Direction.VALUES){
            PathPoint point = openPoint(currentPoint.x + dir.getXOffset(), currentPoint.y + dir.getYOffset(), currentPoint.z + dir.getZOffset());
            if(point != null && !point.visited) {
                pathOptions[i++] = point;
            }
        }

        return i;
    }
}
