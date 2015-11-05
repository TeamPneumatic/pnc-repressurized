package pneumaticCraft.common.ai;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.progwidgets.ICountWidget;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.lib.PneumaticValues;

public class DroneEntityAIInventoryExport extends DroneAIImExBase{

    public DroneEntityAIInventoryExport(IDroneBase drone, ProgWidgetAreaItemBase widget){
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        return export(pos, true);
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        return export(pos, false) && super.doBlockInteraction(pos, distToBlock);
    }

    private boolean export(ChunkPosition pos, boolean simulate){
        IInventory inv = IOHelper.getInventoryForTE(drone.getWorld().getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ));
        if(inv != null) {
            for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
                ItemStack droneStack = drone.getInventory().getStackInSlot(i);
                if(droneStack != null) {
                    if(widget.isItemValidForFilters(droneStack)) {
                        for(int side = 0; side < 6; side++) {
                            droneStack = drone.getInventory().getStackInSlot(i);
                            if(((ISidedWidget)widget).getSides()[side] && droneStack != null) {
                                droneStack = droneStack.copy();
                                int oldCount = droneStack.stackSize;
                                if(((ICountWidget)widget).useCount()) droneStack.stackSize = Math.min(droneStack.stackSize, getRemainingCount());
                                ItemStack remainder = IOHelper.insert(inv, droneStack.copy(), side, simulate);
                                int stackSize = drone.getInventory().getStackInSlot(i).stackSize - (remainder == null ? droneStack.stackSize : droneStack.stackSize - remainder.stackSize);
                                droneStack.stackSize = stackSize;
                                int exportedItems = oldCount - stackSize;
                                if(!simulate) {
                                    drone.getInventory().setInventorySlotContents(i, stackSize > 0 ? droneStack : null);
                                    decreaseCount(exportedItems);
                                }
                                if(simulate && exportedItems > 0) return true;
                                if(!(inv instanceof ISidedInventory)) break; //doing it for every side for no side sensitive inventories would be a waste.
                            }
                        }
                        if(droneStack == null && !simulate) drone.addAir(null, -PneumaticValues.DRONE_USAGE_INV);
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
