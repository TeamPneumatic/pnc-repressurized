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

import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;

public class DroneAIEnergyImport extends DroneAIImExBase<ProgWidgetInventoryBase> {
    public DroneAIEnergyImport(IDroneBase drone, ProgWidgetInventoryBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return importEnergy(pos, false) && super.doBlockInteraction(pos, squareDistToBlock);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return importEnergy(pos, true);
    }

    private boolean importEnergy(BlockPos pos, boolean simulate) {
        boolean didWork = false;
        if (droneIsFull()) {
            abort();
        } else {
            TileEntity te = drone.world().getBlockEntity(pos);
            if (te == null) return false;
            for (Direction face : DirectionUtil.VALUES) {
                if (progWidget.isSideSelected(face)) {
                    didWork = tryImportFromSide(te, face, simulate);
                    if (didWork) break;
                }
            }
        }
        return didWork;
    }

    private boolean tryImportFromSide(TileEntity te, Direction face, boolean simulate) {
        return te.getCapability(CapabilityEnergy.ENERGY, face).map(tileHandler -> {
            int toExtract = tileHandler.extractEnergy(useCount() ? getRemainingCount() : Integer.MAX_VALUE, true);
            int toTransfer = insertToDrone(toExtract, true);
            if (toTransfer > 0) {
                if (!simulate) {
                    decreaseCount(toTransfer);
                    tileHandler.extractEnergy(toTransfer, false);
                    insertToDrone(toTransfer, false);
                }
                return true;
            }
            return false;
        }).orElse(false);
    }

    private int insertToDrone(int maxTransfer, boolean simulate) {
        return drone.getCapability(CapabilityEnergy.ENERGY)
                .map(h -> h.receiveEnergy(maxTransfer, simulate))
                .orElseThrow(RuntimeException::new);
    }

    private boolean droneIsFull() {
        return drone.getCapability(CapabilityEnergy.ENERGY)
                .map(h -> h.getEnergyStored() == h.getMaxEnergyStored())
                .orElseThrow(RuntimeException::new);
    }
}
