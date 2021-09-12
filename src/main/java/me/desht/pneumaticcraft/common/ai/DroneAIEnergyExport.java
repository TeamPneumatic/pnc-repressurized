package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class DroneAIEnergyExport extends DroneAIImExBase<ProgWidgetInventoryBase> {
    public DroneAIEnergyExport(IDroneBase drone, ProgWidgetInventoryBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return exportEnergy(pos, false) && super.doBlockInteraction(pos, squareDistToBlock);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return exportEnergy(pos, true);
    }

    private boolean exportEnergy(BlockPos pos, boolean simulate) {
        boolean didWork = false;
        int energy = drone.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElseThrow(RuntimeException::new);
        if (energy == 0) {
            abort();
        } else {
            TileEntity te = drone.world().getBlockEntity(pos);
            if (te != null) {
                for (Direction face : DirectionUtil.VALUES) {
                    if (progWidget.isSideSelected(face)) {
                        didWork = tryExportToSide(te, face, simulate);
                        if (didWork) break;
                    }
                }
            }
        }
        return didWork;
    }

    private boolean tryExportToSide(TileEntity te, Direction face, boolean simulate) {
        return te.getCapability(CapabilityEnergy.ENERGY, face).map(tileHandler -> {
            int receivable = tileHandler.receiveEnergy(progWidget.useCount() ? getRemainingCount() : Integer.MAX_VALUE, true);
            int toTransfer = extractFromDrone(receivable, true);
            if (toTransfer > 0) {
                if (!simulate) {
                    decreaseCount(toTransfer);
                    extractFromDrone(toTransfer, false);
                    tileHandler.receiveEnergy(toTransfer, false);
                }
                return true;
            }
            return false;
        }).orElse(false);
    }

    private int extractFromDrone(int maxEnergy, boolean simulate) {
        return drone.getCapability(CapabilityEnergy.ENERGY)
                .map(h -> h.extractEnergy(maxEnergy, simulate))
                .orElseThrow(RuntimeException::new);
    }
}
