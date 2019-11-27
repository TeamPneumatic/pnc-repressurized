package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class DroneAILiquidExport extends DroneAIImExBase {

    public DroneAILiquidExport(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return fillTank(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return fillTank(pos, false) && super.doBlockInteraction(pos, distToBlock);
    }

    private boolean fillTank(BlockPos pos, boolean simulate) {
        IFluidTank droneTank = drone.getTank();
        if (droneTank.getFluidAmount() == 0) {
            drone.addDebugEntry("gui.progWidget.liquidExport.debug.emptyDroneTank");
            abort();
            return false;
        } else {
            TileEntity te = drone.world().getTileEntity(pos);
            if (te != null) {
                FluidStack exportedFluid = droneTank.drain(Integer.MAX_VALUE, false);
                if (exportedFluid != null && ((ILiquidFiltered) widget).isFluidValid(exportedFluid.getFluid())) {
                    for (int i = 0; i < 6; i++) {
                        if (((ISidedWidget) widget).getSides()[i] && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.byIndex(i))) {
                            IFluidHandler tank = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.byIndex(i));
                            int filledAmount = tank.fill(exportedFluid, false);
                            if (filledAmount > 0) {
                                if (((ICountWidget) widget).useCount()) {
                                    filledAmount = Math.min(filledAmount, getRemainingCount());
                                }
                                if (!simulate) {
                                    decreaseCount(tank.fill(droneTank.drain(filledAmount, true), true));
                                }
                                return true;
                            }
                        }
                    }
                    drone.addDebugEntry("gui.progWidget.liquidExport.debug.filledToMax", pos);
                } else {
                    drone.addDebugEntry("gui.progWidget.liquidExport.debug.noValidFluid");
                }
            } else if (((ILiquidExport) widget).isPlacingFluidBlocks() && (!((ICountWidget) widget).useCount() || getRemainingCount() >= 1000)) {
                Block fluidBlock = droneTank.getFluid().getFluid().getBlock();
                World w = drone.world();
                if (droneTank.getFluidAmount() >= 1000 && fluidBlock != null && isBlockSuitableForExport(w, pos)) {
                    if (!simulate) {
                        decreaseCount(1000);
                        droneTank.drain(1000, true);
                        w.setBlockState(pos, fluidBlock.getDefaultState());
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isBlockSuitableForExport(World w, BlockPos pos) {
        return !FluidUtils.isSourceBlock(w, pos) && w.getBlockState(pos).getBlock().isReplaceable(w, pos);
    }
}
