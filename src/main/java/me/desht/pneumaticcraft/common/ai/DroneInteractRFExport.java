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

public class DroneInteractRFExport implements ICustomBlockInteract {
    @Override
    public String getName() {
        return "rfExport";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_RF_EX;
    }

    @Override
    public boolean doInteract(BlockPos pos, IDrone drone, IBlockInteractHandler interactHandler, boolean simulate) {
        if (!drone.hasCapability(CapabilityEnergy.ENERGY, null)) return false;

        IEnergyStorage droneStorage = drone.getCapability(CapabilityEnergy.ENERGY, null);
        if (droneStorage.getEnergyStored() == 0) {
            interactHandler.abort();
            return false;
        } else {
            TileEntity te = drone.world().getTileEntity(pos);
            if (te == null) return false;
            for (EnumFacing face : EnumFacing.VALUES) {
                if (te.hasCapability(CapabilityEnergy.ENERGY, face)) {
                    IEnergyStorage teStorage = te.getCapability(CapabilityEnergy.ENERGY, face);
                    int transferredEnergy = droneStorage.extractEnergy(
                            teStorage.receiveEnergy(interactHandler.useCount() ? interactHandler.getRemainingCount() : Integer.MAX_VALUE, true), true);
                    if (transferredEnergy > 0) {
                        if (!simulate) {
                            interactHandler.decreaseCount(transferredEnergy);
                            droneStorage.extractEnergy(transferredEnergy, false);
                            teStorage.receiveEnergy(transferredEnergy, false);
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
        return ItemPlastic.ORANGE;
    }
}
