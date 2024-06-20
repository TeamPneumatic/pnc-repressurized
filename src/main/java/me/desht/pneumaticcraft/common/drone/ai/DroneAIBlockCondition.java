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
import me.desht.pneumaticcraft.common.drone.progwidgets.ICondition;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import net.minecraft.core.BlockPos;

public abstract class DroneAIBlockCondition extends DroneAIBlockInteraction<ProgWidgetAreaItemBase> {

    private boolean result;

    public DroneAIBlockCondition(IDrone drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    public boolean canUse() {
        if (super.canUse()) {
            result = ((ICondition) progWidget).isAndFunction();//set the initial value, so it can be modified by the 'evaluate' method later.
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (evaluate(pos) != ((ICondition) progWidget).isAndFunction()) {
            result = !result;
            if (result) {
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.blockCondition.debug.blockMatches", pos);
            } else {
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.blockCondition.debug.blockDoesNotMatch", pos);
            }
            abort();
        }
        return false;
    }

    @Override
    protected void addEndingDebugEntry() {
        if (result) {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.blockCondition.debug.allBlocksMatch");
        } else {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.blockCondition.debug.noBlocksMatch");
        }
    }

    protected abstract boolean evaluate(BlockPos pos);

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return false;
    }

    public boolean getResult() {
        return result;
    }

}
