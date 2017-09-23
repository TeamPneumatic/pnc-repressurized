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
import net.minecraftforge.items.IItemHandler;

public class DroneEntityAIInventoryImport extends DroneAIImExBase {

    public DroneEntityAIInventoryImport(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return importItems(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return importItems(pos, false) && super.doBlockInteraction(pos, distToBlock);
    }

    private boolean importItems(BlockPos pos, boolean simulate) {
        TileEntity te = drone.world().getTileEntity(pos);
        boolean[] sides = ((ISidedWidget) widget).getSides();
        for (int d = 0; d < sides.length; d++) {
            if (!sides[d]) {
                continue;
            }
            IItemHandler inv = IOHelper.getInventoryForTE(te, EnumFacing.getFront(d));
            if (inv == null) {
                continue;
            }
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    if (widget.isItemValidForFilters(stack)) {
                        ItemStack importedStack = stack.copy();
                        if (((ICountWidget) widget).useCount())
                            importedStack.setCount(Math.min(importedStack.getCount(), getRemainingCount()));
                        ItemStack remainder = IOHelper.insert(drone, importedStack.copy(), EnumFacing.UP, simulate);
                        int removedItems = importedStack.getCount() - remainder.getCount();
                        if (!simulate) {
                            inv.extractItem(i, removedItems, false);
                            decreaseCount(removedItems);
                            drone.addAir(null, -PneumaticValues.DRONE_USAGE_INV);
                            if (((ICountWidget) widget).useCount() && getRemainingCount() <= 0) {
                                return false;
                            }
                        } else if (removedItems > 0) {
                            return true;
                        } else {
                            drone.addDebugEntry("gui.progWidget.inventoryImport.debug.filledToMax", pos);
                        }
                    } else {
                        drone.addDebugEntry("gui.progWidget.inventoryImport.debug.stackdoesntPassFilter", pos);
                    }
                }
            }
        }
//        } else {
//            drone.addDebugEntry("gui.progWidget.inventory.debug.noInventory", pos);
//        }

        return false;
    }

}
