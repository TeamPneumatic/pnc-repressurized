package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class NodeProcessorDrone extends FlyingNodeProcessor {
    @Nullable
    @Override
    protected PathPoint openPoint(int x, int y, int z) {
        return ((EntityDrone) entity).isBlockValidPathfindBlock(new BlockPos(x, y, z)) ? super.openPoint(x, y, z) : null;
    }
}
