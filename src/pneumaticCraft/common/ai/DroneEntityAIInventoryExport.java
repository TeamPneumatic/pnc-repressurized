package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.lib.PneumaticValues;

public class DroneEntityAIInventoryExport extends EntityAIBase{
    private final EntityDrone drone;
    private final double speed;
    private final ProgWidgetAreaItemBase exportWidget;
    private TileEntity targetInventory;
    private final DistanceTileEntitySorter closestTileEntitySorter;
    private final Set<ChunkPosition> validArea;

    public DroneEntityAIInventoryExport(EntityDrone drone, double speed,
            ProgWidgetAreaItemBase progWidgetInventoryExport){
        this.drone = drone;
        this.speed = speed;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
        exportWidget = progWidgetInventoryExport;
        validArea = exportWidget.getArea();
        closestTileEntitySorter = new DistanceTileEntitySorter(drone);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        List<Integer> validDroneStacks = new ArrayList<Integer>();//list of inventory indexes of valid exportable stacks.
        for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
            if(drone.getInventory().getStackInSlot(i) != null && exportWidget.isItemValidForFilters(drone.getInventory().getStackInSlot(i))) {
                validDroneStacks.add(i);
            }
        }
        if(validDroneStacks.isEmpty()) return false;

        List<IInventory> inventories = new ArrayList<IInventory>();
        for(TileEntity te : (List<TileEntity>)drone.worldObj.loadedTileEntityList) {
            if(te instanceof IInventory && validArea.contains(new ChunkPosition(te.xCoord, te.yCoord, te.zCoord))) {
                inventories.add((IInventory)te);
            }
        }

        Collections.sort(inventories, closestTileEntitySorter);
        for(IInventory inv : inventories) {
            Set<Integer> accessibleSlots = PneumaticCraftUtils.getAccessibleSlotsForInventoryAndSides(inv, ((ISidedWidget)exportWidget).getSides());
            for(Integer slot : accessibleSlots) {
                ItemStack stack = inv.getStackInSlot(slot);
                for(Integer j : validDroneStacks) {
                    ItemStack droneStack = drone.getInventory().getStackInSlot(j); //droneStack is already null checked at the beginning of the method.
                    if(stack == null || stack.getItem() == droneStack.getItem() && stack.stackSize < stack.getMaxStackSize()) {
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
        return false;

    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting(){
        if(targetInventory.isInvalid()) return false;
        if(targetInventory.getDistanceFrom(drone.posX, drone.posY + drone.height / 2, drone.posZ) < 1.5) {
            for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
                ItemStack droneStack = drone.getInventory().getStackInSlot(i);
                if(droneStack != null && exportWidget.isItemValidForFilters(droneStack)) {
                    for(int side = 0; side < 6; side++) {
                        droneStack = drone.getInventory().getStackInSlot(i);
                        if(((ISidedWidget)exportWidget).getSides()[side] && droneStack != null) {
                            drone.getInventory().setInventorySlotContents(i, PneumaticCraftUtils.exportStackToInventory((IInventory)targetInventory, droneStack, ForgeDirection.getOrientation(side)));
                            if(!(targetInventory instanceof ISidedInventory)) break; //doing it for every side for no side sensitive inventories would be a waste.
                        }
                    }
                    if(droneStack == null) drone.addAir(null, -PneumaticValues.DRONE_USAGE_INV);
                }
            }
            return false;
        }
        return !drone.getNavigator().noPath();
    }
}
