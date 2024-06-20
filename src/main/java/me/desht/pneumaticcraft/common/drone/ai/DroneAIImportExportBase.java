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

import me.desht.pneumaticcraft.api.drone.IBlockInteractHandler;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetInventoryBase;
import net.minecraft.core.BlockPos;

public abstract class DroneAIImportExportBase<W extends ProgWidgetInventoryBase>
        extends DroneAIBlockInteraction<W>
        implements IBlockInteractHandler {
    private int transportCount;

    protected DroneAIImportExportBase(IDrone drone, W widget) {
        super(drone, widget);
        transportCount = widget.getCount();
    }

    @Override
    public boolean canUse() {
        boolean countReached = transportCount <= 0;
        transportCount = progWidget.getCount();
        return !(countReached && useCount()) && super.canUse();
    }

    @Override
    public void decreaseCount(int count) {
        transportCount -= count;
    }

    @Override
    public int getRemainingCount() {
        return transportCount;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return !useCount() || transportCount > 0;
    }

    @Override
    public boolean[] getSides() {
        return progWidget.getSides();
    }

    @Override
    public boolean useCount() {
        return progWidget.useCount();
    }

}
