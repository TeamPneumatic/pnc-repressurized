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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.progwidgets.IAreaProvider;
import me.desht.pneumaticcraft.common.drone.progwidgets.IGotoWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class DroneEntityAIGoToLocation extends Goal {
    protected final IDrone drone;
    private final ProgWidget gotoWidget;
    private final ChunkPositionSorter positionSorter;
    private final List<BlockPos> validArea;

    public DroneEntityAIGoToLocation(IDrone drone, ProgWidget gotoWidget) {
        this.drone = drone;
        setFlags(EnumSet.allOf(Flag.class)); // so it won't run along with other AI tasks.
        this.gotoWidget = gotoWidget;
        Set<BlockPos> set = new HashSet<>();
        ((IAreaProvider) gotoWidget).getArea(set);
        validArea = new ArrayList<>(set);
        positionSorter = new ChunkPositionSorter(drone);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean canUse() {
        validArea.sort(positionSorter);
        for (BlockPos c : validArea) {
            // 0.75 is the squared dist from a block corner to its center (0.5^2 + 0.5^2 + 0.5^2)
            if (drone.getDronePos().distanceToSqr(new Vec3(c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5)) < 0.75)
                return false;
            if (drone.getPathNavigator().moveToXYZ(c.getX(), c.getY(), c.getZ())) {
                return !((IGotoWidget) gotoWidget).doneWhenDeparting();
            }
        }
        boolean teleport = drone.getPathNavigator().isGoingToTeleport();
        if (teleport) {
            return true;
        } else {
            for (BlockPos c : validArea) {
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.goto.debug.cantNavigate", c);
            }
            return false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean canContinueToUse() {
        return !drone.getPathNavigator().hasNoPath();
    }
}
