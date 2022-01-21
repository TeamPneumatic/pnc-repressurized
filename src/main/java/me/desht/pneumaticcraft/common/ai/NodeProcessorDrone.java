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

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

public class NodeProcessorDrone extends FlyNodeEvaluator {
    @Nullable
    @Override
    protected Node getNode(int x, int y, int z) {
        return ((EntityDrone) mob).isBlockValidPathfindBlock(new BlockPos(x, y, z)) ? super.getNode(x, y, z) : null;
    }

    /**
     * Override this, because the super method adds diagonals, this is fancy but doesn't work well with drones (drones stuck behind a wall).
     */
    @Override
    public int getNeighbors(Node[] pathOptions, Node currentPoint) {
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
