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
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetForEachCoordinate;
import net.minecraft.core.BlockPos;

public class DroneAIForEachCoordinate extends DroneAIBlockInteraction<ProgWidgetForEachCoordinate> {

    private BlockPos curCoord;

    public DroneAIForEachCoordinate(IDrone drone, ProgWidgetForEachCoordinate widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (progWidget.isValidPosition(pos)) {
            curCoord = pos;
            abort();
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return false;
    }

    public BlockPos getCurCoord() {
        return curCoord;
    }

    @Override
    protected void addEndingDebugEntry() {
        // nothing
    }
}
