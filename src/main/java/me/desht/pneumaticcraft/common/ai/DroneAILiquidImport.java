package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ICountWidget;
import me.desht.pneumaticcraft.common.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class DroneAILiquidImport extends DroneAIImExBase {

    public DroneAILiquidImport(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return emptyTank(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return emptyTank(pos, false) && super.doBlockInteraction(pos, distToBlock);
    }

    private boolean emptyTank(BlockPos pos, boolean simulate) {
        if (drone.getTank().getFluidAmount() == drone.getTank().getCapacity()) {
            drone.addDebugEntry("gui.progWidget.liquidImport.debug.fullDroneTank");
            abort();
            return false;
        } else {
            TileEntity te = drone.world().getTileEntity(pos);
            if (te != null) {
                for (int i = 0; i < 6; i++) {
                    if (((ISidedWidget) widget).getSides()[i] && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.getFront(i))) {
                        IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.getFront(i));
                        FluidStack importedFluid = handler.drain(Integer.MAX_VALUE, false);
                        if (importedFluid != null && ((ILiquidFiltered) widget).isFluidValid(importedFluid.getFluid())) {
                            int filledAmount = drone.getTank().fill(importedFluid, false);
                            if (filledAmount > 0) {
                                if (((ICountWidget) widget).useCount())
                                    filledAmount = Math.min(filledAmount, getRemainingCount());
                                if (!simulate) {
                                    decreaseCount(drone.getTank().fill(handler.drain(filledAmount, true), true));
                                }
                                return true;
                            }
                        }
                    }
                }
                drone.addDebugEntry("gui.progWidget.liquidImport.debug.emptiedToMax", pos);
            } else if (!((ICountWidget) widget).useCount() || getRemainingCount() >= 1000) {
                Fluid fluid = FluidRegistry.lookupFluidForBlock(drone.world().getBlockState(pos).getBlock());
                if (fluid != null && ((ILiquidFiltered) widget).isFluidValid(fluid) && drone.getTank().fill(new FluidStack(fluid, 1000), false) == 1000 && FluidUtils.isSourceBlock(drone.world(), pos)) {
                    if (!simulate) {
                        decreaseCount(1000);
                        drone.getTank().fill(new FluidStack(fluid, 1000), true);
                        drone.world().setBlockToAir(pos);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
