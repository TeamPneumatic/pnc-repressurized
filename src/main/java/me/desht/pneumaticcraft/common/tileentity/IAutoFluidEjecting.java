package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import static net.minecraftforge.fluids.FluidAttributes.BUCKET_VOLUME;

interface IAutoFluidEjecting {
    default void autoExportFluid(TileEntityBase te) {
        te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(handler -> {
            FluidStack toDrain = handler.drain(BUCKET_VOLUME, FluidAction.SIMULATE);
            if (!toDrain.isEmpty()) {
                Direction ejectDir = te.getUpgradeCache().getEjectDirection();
                if (ejectDir != null) {
                    tryEjectLiquid(te, handler, ejectDir, toDrain.getAmount());
                } else {
                    for (Direction d : Direction.VALUES) {
                        toDrain.setAmount(toDrain.getAmount() - tryEjectLiquid(te, handler, d, toDrain.getAmount()));
                        if (toDrain.getAmount() <= 0) break;
                    }
                }
            }
        });
    }

    default int tryEjectLiquid(TileEntityBase te, IFluidHandler handler, Direction dir, int amount) {
        TileEntity teNeighbour = te.getTileCache()[dir.ordinal()].getTileEntity();
        if (teNeighbour != null) {
            return teNeighbour.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite()).map(destHandler -> {
                FluidStack fluidStack = FluidUtil.tryFluidTransfer(destHandler, handler, amount, true);
                return fluidStack.getAmount();
            }).orElse(0);
        }
        return 0;
    }
}
