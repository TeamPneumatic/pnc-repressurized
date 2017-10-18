package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.SwimNodeProcessor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class NodeProcessorDrone extends SwimNodeProcessor {
    //TODO 1.8 test if it works
    @Override
    public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
        EntityDrone drone = (EntityDrone) entity;
        int curIndex = 0;

        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos pos = new BlockPos(currentPoint.x + dir.getFrontOffsetX(), currentPoint.y + dir.getFrontOffsetY(), currentPoint.z + dir.getFrontOffsetZ());
            if (drone.isBlockValidPathfindBlock(pos)) {
                PathPoint pathpoint = openPoint(pos.getX(), pos.getY(), pos.getZ());
                if (!pathpoint.visited && pathpoint.distanceTo(targetPoint) < maxDistance)
                    pathOptions[curIndex++] = pathpoint;
            }
        }
        return curIndex;
    }
}
