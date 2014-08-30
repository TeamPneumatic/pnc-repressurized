package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;

public class DroneEntityAIInventoryImport extends EntityAIBase{
    private final EntityDrone drone;
    private final double speed;
    private final ProgWidgetAreaItemBase importWidget;
    private TileEntity targetInventory;
    private final DistanceTileEntitySorter closestTileEntitySorter;
    private final Set<ChunkPosition> validArea;

    public DroneEntityAIInventoryImport(EntityDrone drone, double speed, ProgWidgetAreaItemBase progWidgetImport){
        this.drone = drone;
        this.speed = speed;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
        importWidget = progWidgetImport;
        validArea = importWidget.getArea();
        closestTileEntitySorter = new DistanceTileEntitySorter(drone);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        List<IInventory> inventories = new ArrayList<IInventory>();
        for(ChunkPosition pos : validArea) {
            TileEntity te = drone.worldObj.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
            if(te instanceof IInventory) {
                inventories.add((IInventory)te);
            }
        }

        Collections.sort(inventories, closestTileEntitySorter);

        for(IInventory inv : inventories) {
            Set<Integer> accessibleSlots = PneumaticCraftUtils.getAccessibleSlotsForInventoryAndSides(inv, ((ISidedWidget)importWidget).getSides());
            for(Integer i : accessibleSlots) {
                ItemStack stack = inv.getStackInSlot(i);
                if(stack != null && importWidget.isItemValidForFilters(stack)) {
                    for(int j = 0; j < drone.getInventory().getSizeInventory(); j++) {
                        ItemStack droneStack = drone.getInventory().getStackInSlot(j);
                        if(droneStack == null || PneumaticCraftUtils.areStacksEqual(droneStack, stack, true, true, false, false) && droneStack.stackSize < droneStack.getMaxStackSize()) {
                            TileEntity te = (TileEntity)inv;
                            for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                                if(drone.isBlockValidPathfindBlock(te.xCoord + dir.offsetX, te.yCoord + dir.offsetY, te.zCoord + dir.offsetZ) && PneumaticCraftUtils.tryNavigateToXYZ(drone, te.xCoord + dir.offsetX, te.yCoord + dir.offsetY + 0.5, te.zCoord + dir.offsetZ, speed)) {
                                    targetInventory = te;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;

    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting(){
        if(targetInventory.isInvalid()) return false;
        if(targetInventory.getDistanceFrom(drone.posX, drone.posY + drone.height / 2, drone.posZ) < 1.5) {
            IInventory inv = (IInventory)targetInventory;
            Set<Integer> accessibleSlots = PneumaticCraftUtils.getAccessibleSlotsForInventoryAndSides(inv, ((ISidedWidget)importWidget).getSides());
            for(Integer i : accessibleSlots) {
                ItemStack stack = inv.getStackInSlot(i);
                if(stack != null && importWidget.isItemValidForFilters(stack)) {
                    inv.setInventorySlotContents(i, PneumaticCraftUtils.exportStackToInventory(drone.getInventory(), stack, ForgeDirection.UP));
                    drone.addAir(null, -PneumaticValues.DRONE_USAGE_INV);
                }
            }
            return false;
        }
        return !drone.getNavigator().noPath();
    }

}
