package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ICountWidget;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class DroneEntityAIInventoryExport extends DroneAIImExBase {

    public DroneEntityAIInventoryExport(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return export(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return export(pos, false) && super.doBlockInteraction(pos, distToBlock);
    }

    private boolean export(BlockPos pos, boolean simulate) {
        TileEntity te = drone.world().getTileEntity(pos);
        if (te != null) {
            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                ItemStack droneStack = drone.getInv().getStackInSlot(i);
                if (!droneStack.isEmpty()) {
                    if (widget.isItemValidForFilters(droneStack)) {
                        for (int side = 0; side < 6; side++) {
                            if (((ISidedWidget) widget).getSides()[side]) {
                                droneStack = droneStack.copy();
                                int oldCount = droneStack.getCount();
                                if (((ICountWidget) widget).useCount()) {
                                    droneStack.setCount(Math.min(droneStack.getCount(), getRemainingCount()));
                                }
                                ItemStack remainder = IOHelper.insert(te, droneStack.copy(), EnumFacing.getFront(side), simulate);
                                int stackSize = drone.getInv().getStackInSlot(i).getCount() - (remainder.isEmpty() ? droneStack.getCount() : droneStack.getCount() - remainder.getCount());
                                droneStack.setCount(stackSize);
                                int exportedItems = oldCount - stackSize;
                                if (!simulate) {
                                    drone.getInv().setStackInSlot(i, stackSize > 0 ? droneStack : ItemStack.EMPTY);
                                    decreaseCount(exportedItems);
                                }
                                if (simulate && exportedItems > 0) return true;
//                                if (!(inv instanceof ISidedInventory))
//                                    break; //doing it for every side for no side sensitive inventories would be a waste.
                            }
                        }
                        if (droneStack.isEmpty() && !simulate) drone.addAir(null, -PneumaticValues.DRONE_USAGE_INV);
                        else drone.addDebugEntry("gui.progWidget.inventoryExport.debug.filledToMax", pos);
                    } else {
                        drone.addDebugEntry("gui.progWidget.inventoryExport.debug.stackdoesntPassFilter", pos);
                    }
                }
            }
        } else {
            drone.addDebugEntry("gui.progWidget.inventory.debug.noInventory", pos);
        }
        return false;
    }
}
