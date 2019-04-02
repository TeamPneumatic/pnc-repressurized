package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

interface IAutoFluidEjecting {
    default void autoExportFluid(TileEntityBase te) {
        if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            FluidStack toDrain = handler.drain(1000, false);
            if (toDrain != null && toDrain.amount > 0) {
                EnumFacing ejectDir = te.getUpgradeCache().getEjectDirection();
                if (ejectDir != null) {
                    tryEjectLiquid(te, handler, ejectDir, toDrain.amount);
                } else {
                    for (EnumFacing d : EnumFacing.VALUES) {
                        toDrain.amount -= tryEjectLiquid(te, handler, d, toDrain.amount);
                        if (toDrain.amount <= 0) break;
                    }
                }
            }
        }
    }

    default int tryEjectLiquid(TileEntityBase te, IFluidHandler handler, EnumFacing dir, int amount) {
        TileEntity teNeighbour = te.getTileCache()[dir.ordinal()].getTileEntity();
        if (teNeighbour != null && teNeighbour.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite())) {
            IFluidHandler destHandler = teNeighbour.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
            FluidStack sent = FluidUtil.tryFluidTransfer(destHandler, handler, amount, true);
            return sent == null ? 0 : sent.amount;
        }
        return 0;
    }
}
