package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.IBlockInteractHandler;
import me.desht.pneumaticcraft.api.drone.ICustomBlockInteract;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class DroneInteractRFImport implements ICustomBlockInteract {
    @Override
    public String getName() {
        return "rfImport";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_RF_IM;
    }

    @Override
    public boolean doInteract(BlockPos pos, IDrone drone, IBlockInteractHandler interactHandler, boolean simulate) {
        return drone.getCapability(CapabilityEnergy.ENERGY)
                .map(droneStorage -> tryTransfer(pos, drone, interactHandler, simulate, droneStorage))
                .orElse(false);
    }

    private boolean tryTransfer(BlockPos pos, IDrone drone, IBlockInteractHandler interactHandler, boolean simulate, IEnergyStorage droneStorage) {
        boolean didWork = false;

        if (droneStorage.getEnergyStored() == droneStorage.getMaxEnergyStored()) {
            interactHandler.abort();
        } else {
            TileEntity te = drone.world().getTileEntity(pos);
            if (te == null) return false;
            for (Direction face : Direction.VALUES) {
                if (interactHandler.isSideAccessible(face)) {
                    didWork = te.getCapability(CapabilityEnergy.ENERGY, face).map(teStorage -> {
                        int extractedEnergy = teStorage.extractEnergy(interactHandler.useCount() ?
                                interactHandler.getRemainingCount() : Integer.MAX_VALUE, true);
                        int transferredEnergy = droneStorage.receiveEnergy(extractedEnergy, true);
                        if (transferredEnergy > 0) {
                            if (!simulate) {
                                interactHandler.decreaseCount(transferredEnergy);
                                droneStorage.receiveEnergy(transferredEnergy, false);
                                teStorage.extractEnergy(transferredEnergy, false);
                            }
                            return true;
                        }
                        return false;
                    }).orElse(false);
                }
                if (didWork) break;
            }
        }
        return didWork;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.BLUE;
    }
}
