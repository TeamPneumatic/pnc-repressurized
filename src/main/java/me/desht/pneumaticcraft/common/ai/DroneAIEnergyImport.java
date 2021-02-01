package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
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
            TileEntity te = drone.world().getTileEntity(pos);
            if (te == null) return false;
            for (Direction face : Direction.VALUES) {
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
