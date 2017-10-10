package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.IBlockInteractHandler;
import me.desht.pneumaticcraft.api.drone.ICustomBlockInteract;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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
        if (!drone.hasCapability(CapabilityEnergy.ENERGY, null)) return false;

        IEnergyStorage droneStorage = drone.getCapability(CapabilityEnergy.ENERGY, null);
        if (droneStorage.getEnergyStored() == droneStorage.getMaxEnergyStored()) {
            interactHandler.abort();
            return false;
        } else {
            TileEntity te = drone.world().getTileEntity(pos);
            if (te == null) return false;
            for (EnumFacing face : EnumFacing.values()) {
                if (te.hasCapability(CapabilityEnergy.ENERGY, face)) {
                    IEnergyStorage teStorage = te.getCapability(CapabilityEnergy.ENERGY, face);
                    int transferredEnergy = droneStorage.receiveEnergy(
                            teStorage.extractEnergy(interactHandler.useCount() ? interactHandler.getRemainingCount() : Integer.MAX_VALUE, true), true);
                    if (transferredEnergy > 0) {
                        if (!simulate) {
                            interactHandler.decreaseCount(transferredEnergy);
                            droneStorage.receiveEnergy(transferredEnergy, false);
                            teStorage.extractEnergy(transferredEnergy, false);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.BLUE;
    }
}
