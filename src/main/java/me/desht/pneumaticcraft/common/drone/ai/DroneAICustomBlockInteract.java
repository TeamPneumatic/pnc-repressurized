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

import me.desht.pneumaticcraft.api.drone.ICustomBlockInteract;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetInventoryBase;
import net.minecraft.core.BlockPos;

public class DroneAICustomBlockInteract extends DroneAIImportExportBase<ProgWidgetInventoryBase> {
    private final ICustomBlockInteract blockInteractor;

    public DroneAICustomBlockInteract(IDrone drone, ProgWidgetInventoryBase widget,
                                      ICustomBlockInteract blockInteractor) {
        super(drone, widget);
        this.blockInteractor = blockInteractor;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return blockInteractor.doInteract(pos, drone, this, false) && super.doBlockInteraction(pos, squareDistToBlock);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return blockInteractor.doInteract(pos, drone, this, true);
    }
}
