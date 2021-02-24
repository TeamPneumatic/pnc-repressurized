package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public class DroneEntityAIInventoryImport extends DroneAIImExBase<ProgWidgetInventoryBase> {

    public DroneEntityAIInventoryImport(IDroneBase drone, ProgWidgetInventoryBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return importItems(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return importItems(pos, false) && super.doBlockInteraction(pos, squareDistToBlock);
    }

    private boolean importItems(BlockPos pos, boolean simulate) {
        TileEntity te = drone.world().getTileEntity(pos);
        boolean imported = false;
        for (Direction dir : DirectionUtil.VALUES) {
            if (progWidget.isSideSelected(dir)) {
                imported = IOHelper.getInventoryForTE(te, dir).map(inv -> tryImport(inv, pos, simulate)).orElse(false);
                if (imported) break;
            }
        }
        return imported;
    }

    private boolean tryImport(IItemHandler inv, BlockPos pos, boolean simulate) {
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (progWidget.isItemValidForFilters(stack)) {
                    ItemStack importedStack = inv.extractItem(i, stack.getCount(), true);
                    if (importedStack.isEmpty()) {
                        continue;
                    }
                    importedStack = importedStack.copy();
                    if (progWidget.useCount()) {
                        importedStack.setCount(Math.min(importedStack.getCount(), getRemainingCount()));
                    }
                    ItemStack remainder = IOHelper.insert(drone, importedStack, Direction.UP, simulate);
                    int removedItems = importedStack.getCount() - remainder.getCount();
                    if (!simulate) {
                        inv.extractItem(i, removedItems, false);
                        decreaseCount(removedItems);
                        drone.addAirToDrone(-PneumaticValues.DRONE_USAGE_INV);
                        if (progWidget.useCount() && getRemainingCount() <= 0) {
                            return false;
                        }
                    } else if (removedItems > 0) {
                        return true;
                    } else {
                        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.inventoryImport.debug.filledToMax", pos);
                    }
                } else {
                    drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.inventoryImport.debug.stackdoesntPassFilter", pos);
                }
            }
        }
        return false;
    }

}
