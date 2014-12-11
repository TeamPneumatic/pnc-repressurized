package pneumaticCraft.common.tileentity;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.recipe.AssemblyRecipe;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.LazySynced;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.TileEntityConstants;

public class TileEntityAssemblyIOUnit extends TileEntityAssemblyRobot{
    @DescSynced
    public boolean shouldClawClose;
    @DescSynced
    @LazySynced
    public float clawProgress;
    public float oldClawProgress;
    @DescSynced
    public ItemStack[] inventory = new ItemStack[1];
    private int pickUpPlatformStackStep;
    public int feedPlatformStep;
    private int exportHeldItemStep;
    private List<AssemblyRecipe> recipeList;
    private ItemStack searchedItemStack;
    private byte state = 0;
    private byte tickCounter = 0;
    
    private final static byte SLEEP_TICKS = 50;
    
    private final static byte STATE_IDLE = 0;
    private final static byte STATE_SEARCH_SRC = 1;
    private final static byte STATE_SEARCH_DROPOFF = 5;
    private final static byte STATE_MAX = 127;
    
    public boolean pickupItem(List<AssemblyRecipe> list) {   	
    	this.recipeList = list;
    	
    	if(this.state == STATE_IDLE)
    		this.state++;
    	
    	return((this.state > STATE_SEARCH_SRC)
    			&& (this.state != STATE_SEARCH_DROPOFF)
    			&& (this.state < STATE_MAX)); // will not use air while waiting for item/inventory to be available
    }
    
    private boolean gotoIdlePos() {
    	this.gotoHomePosition();
    	return(this.isDone());
    }
    
    private boolean findPickupLocation() {
    	if(shouldSleep())
    		return(false);
    	
        ForgeDirection[] inventoryDir = null;
        
        if(this.isImportUnit()) {
        	this.searchedItemStack = null;
        	if(this.recipeList != null) {
        		for(AssemblyRecipe recipe : recipeList) {
        			inventoryDir = getInventoryDirectionForItem(recipe.getInput());
        			if(inventoryDir != null) {
        				this.searchedItemStack = recipe.getInput();
        				break;
        			}
        		}
        	}
        } else {
        	inventoryDir = getPlatformDirection();        	
        }
        
        this.targetDirection = inventoryDir;
        
        return(this.targetDirection != null);
    }

	private boolean shouldSleep() {
		if((this.tickCounter > 0) && this.tickCounter++ < SLEEP_TICKS) {
    		return(true);
    	} else {
    		this.tickCounter = 0;
    		return(false);
    	}
	}
	
	private void sleepBeforeNextSearch() {
		this.tickCounter = 1;
	}
    
    private boolean findDropOffLoation() {
    	if(shouldSleep())
    		return(false);

    	ForgeDirection[] inventoryDir = null;
        
        if(this.isImportUnit()) {
        	inventoryDir = getPlatformDirection();
        } else {
        	inventoryDir = getExportLocationForItem(inventory[0]);
        }
    
        this.targetDirection = inventoryDir;
        
        if(this.targetDirection == null) {
        	sleepBeforeNextSearch();
        	
        	return(false);
        } else
        	return(true);        
    }
    
    private boolean getItemFromCurrentDirection(){
    	TileEntity tile = getTileEntityForCurrentDirection();

		boolean extracted = false;

		if(this.isImportUnit()) {
			if(tile instanceof IInventory) {
				IInventory inv = (IInventory)tile;
				for(int i = 0; i < inv.getSizeInventory(); i++) {
					if(inv.getStackInSlot(i) != null && inv.getStackInSlot(i).isItemEqual(searchedItemStack)) {
						if(inventory[0] == null) {
							inventory[0] = inv.decrStackSize(i, 1);
						} else {
							inv.decrStackSize(i, 1);
							inventory[0].stackSize++;
						}
						extracted = true;
						break;
					}
				}
			}
		} else {
	    	if(tile instanceof TileEntityAssemblyPlatform) {
	    		
	    		TileEntityAssemblyPlatform plat = (TileEntityAssemblyPlatform)tile;
	    		
	    		if(true) { // TODO: check if we can pickup the item
	    			inventory[0] = plat.getHeldStack();
	    			plat.setHeldStack(null);
	    			extracted = (inventory[0] != null);
	    		}
	    	}			
		}
    	
    	return(extracted);
    }
    
    private boolean putItemToCurrentDirection() {    	    	
    	if(this.isImportUnit()) {
    		TileEntity tile = getTileEntityForCurrentDirection();
    		if(tile instanceof TileEntityAssemblyPlatform) {

    			TileEntityAssemblyPlatform plat = (TileEntityAssemblyPlatform)tile;

    			if(plat.isReadyForItem()) {
    				plat.setHeldStack(inventory[0]);
    				inventory[0] = null;
    				return(true);
    			}
    		}
    	} else {
            IInventory inv = getInventoryForCurrentDirection();
            if(inv != null) {
                int startSize = inventory[0].stackSize;
                for(int i = 0; i < 6; i++) {
                    inventory[0] = PneumaticCraftUtils.exportStackToInventory(inv, inventory[0], ForgeDirection.getOrientation(i));
                    if(inventory[0] == null) break;
                }
                if(inventory[0] == null || startSize != inventory[0].stackSize) sendDescriptionPacket();
            }
            
            return(inventory[0] == null);
    	}
    	
    	return(false);
/*
    	
    	
    	if(platform != null) {
platform.setHeldStack(inventory[0]);
inventory[0] = null;
}
if(platformDir != null) plat = getTileEntityForDirection(platformDir[0], platformDir[1]);
TileEntityAssemblyPlatform platform = null;
if(plat instanceof TileEntityAssemblyPlatform) {
platform = (TileEntityAssemblyPlatform)plat;
}
*/
    }
    
    @Override
    public void updateEntity() {
        super.updateEntity();
        
        if(!worldObj.isRemote){
        	
//            moveClaw();
            
            switch(this.state) {

            case STATE_IDLE:
            	break;
            case STATE_SEARCH_SRC:
            	if(findPickupLocation())
            		this.state++;
            	break;
            // rise to the right height for target location
            case 2: // for pickup 
            case 6: // for drop-off
            	if(hoverOverTarget())
            		this.state++;
            	break;
           	// turn and move to target
            case 3: // for pickup 
            case 7: // for drop-off
            	if(gotoTarget())
            		this.state++;
            	break;
            case 4: // pickup item
            	if(getItemFromCurrentDirection())
            		this.state++;
            	break;
            case STATE_SEARCH_DROPOFF:
            	if(findDropOffLoation())
            		this.state++;
            	break;
            case 8: // drop off item
            	if(putItemToCurrentDirection())
            		this.state++;
            	break;
            case 9:
            	if(gotoIdlePos())
            		this.state = 0;
            case STATE_MAX: // this will be set if we encounter an unknown state; prevents log-spam that would result from default-case
            	break;
            default:
            	System.out.printf("unexpected state: {0}\n", this.state);
            	this.state = STATE_MAX;
            	break;
            }
        }

        /*
        hoverOverNeighbour(inventoryDir[0], inventoryDir[1]);
        shouldClawClose = false;
        break;

        slowMode = true;
        gotoNeighbour(inventoryDir[0], inventoryDir[1]);
        */

        
        /*
        if(!worldObj.isRemote) {
            if(!shouldClawClose && clawProgress == 0F && isDoneInternal()) {
                if(isExportUnit() && inventory[0] != null) {//when in export mode, auto eject.
                    IInventory inv = getInventoryForCurrentDirection();
                    if(inv != null) {
                        int startSize = inventory[0].stackSize;
                        for(int i = 0; i < 6; i++) {
                            inventory[0] = PneumaticCraftUtils.exportStackToInventory(inv, inventory[0], ForgeDirection.getOrientation(i));
                            if(inventory[0] == null) break;
                        }
                        if(inventory[0] == null || startSize != inventory[0].stackSize) sendDescriptionPacket();
                    }
                }
            }
            if(pickUpPlatformStackStep > 0 && isExportUnit()) {
                ForgeDirection[] platformDir = getPlatformDirection();
                TileEntity tile = getTileEntityForCurrentDirection();
                TileEntityAssemblyPlatform platform = null;
                if(tile instanceof TileEntityAssemblyPlatform) {
                    platform = (TileEntityAssemblyPlatform)tile;
                }
                if(platformDir == null) pickUpPlatformStackStep = 1;
                switch(pickUpPlatformStackStep){
                    case 1:
                        slowMode = false;
                        gotoHomePosition();
                        break;
                    case 2:
                        hoverOverNeighbour(platformDir[0], platformDir[1]);
                        shouldClawClose = false;
                        break;
                    case 3:
                        slowMode = true;
                        gotoNeighbour(platformDir[0], platformDir[1]);
                        break;
                    case 4:
                        TileEntity te = getTileEntityForCurrentDirection();
                        if(te instanceof TileEntityAssemblyPlatform) {
                            inventory[0] = ((TileEntityAssemblyPlatform)te).getHeldStack();
                            ((TileEntityAssemblyPlatform)te).setHeldStack(null);
                        }
                        break;

                    case 5:
                        if(platform != null) {
                            platform.openClaw();
                        }
                        break;
                    case 6:
                        shouldClawClose = true;
                        break;
                    case 7:
                        hoverOverNeighbour(platformDir[0], platformDir[1]);
                        break;
                }
                if(isDoneInternal()) {
                    pickUpPlatformStackStep++;
                    if(pickUpPlatformStackStep > 7) {
                        pickUpPlatformStackStep = 0;
                        slowMode = false;
                    }
                }
            } else if(exportHeldItemStep > 0 && isExportUnit()) {
                ForgeDirection[] chestLocation = getExportLocationForItem(inventory[0]);
                if(chestLocation != null || exportHeldItemStep > 3) {
                    switch(exportHeldItemStep){
                        case 1:
                            slowMode = false;
                            shouldClawClose = true;
                            break;
                        case 2:
                            hoverOverNeighbour(chestLocation[0], chestLocation[1]);
                            break;
                        case 3:
                            slowMode = true;
                            gotoNeighbour(chestLocation[0], chestLocation[1]);
                            break;
                        case 4:
                            shouldClawClose = false;
                            break;
                        case 5:
                        	slowMode = false;
                            gotoHomePosition();
                            break;
                    }
                    if(isDoneInternal() && (exportHeldItemStep != 4 || inventory[0] == null)) {
                        exportHeldItemStep++;
                        if(exportHeldItemStep > 5) {
                            exportHeldItemStep = 0;
                        }
                    }
                } else if(inventory[0] == null) {
                    gotoHomePosition();
                    slowMode = false;
                    exportHeldItemStep = 0;
                }
            } else if(feedPlatformStep > 0 && isImportUnit() && recipeList != null) {
            	// TODO BUG FIXME recipeList will be null if we're in going-home-mode
            	// because program was removed from controller and the world is reloaded
                ForgeDirection[] inventoryDir = null;
                searchedItemStack = null;
                for(AssemblyRecipe recipe : recipeList) {
                    inventoryDir = getInventoryDirectionForItem(recipe.getInput());
                    if(inventoryDir != null) {
                        searchedItemStack = recipe.getInput();
                        break;
                    }
                }

           
                
                if(searchedItemStack != null || feedPlatformStep != 4) {
                    ForgeDirection[] platformDir = getPlatformDirection();
                    TileEntity tile = getTileEntityForCurrentDirection();
                    TileEntity plat = null;
                    if(platformDir != null) plat = getTileEntityForDirection(platformDir[0], platformDir[1]);
                    TileEntityAssemblyPlatform platform = null;
                    if(plat instanceof TileEntityAssemblyPlatform) {
                        platform = (TileEntityAssemblyPlatform)plat;
                    }
                    //  System.out.println("inventory: " + inventory[0] + ", feedPlatformStep = " + feedPlatformStep + " tile entity: " + this);
                    if(feedPlatformStep < 4 && inventoryDir == null) feedPlatformStep = 1;
                    if(feedPlatformStep > 6 && (platformDir == null || platform == null)) feedPlatformStep = 6;
                    switch(feedPlatformStep){
                        case 1:
                            gotoHomePosition();
                            break;
                        case 2:
                            hoverOverNeighbour(inventoryDir[0], inventoryDir[1]);
                            shouldClawClose = false;
                            break;
                        case 3:
                            slowMode = true;
                            gotoNeighbour(inventoryDir[0], inventoryDir[1]);
                            break;
                        case 4:
                            if(tile instanceof IInventory) {
                                IInventory inv = (IInventory)tile;
                                boolean extracted = false;
                                for(int i = 0; i < inv.getSizeInventory(); i++) {
                                    if(inv.getStackInSlot(i) != null && inv.getStackInSlot(i).isItemEqual(searchedItemStack)) {
                                        if(inventory[0] == null) {
                                            inventory[0] = inv.decrStackSize(i, 1);
                                        } else {
                                            inv.decrStackSize(i, 1);
                                            inventory[0].stackSize++;
                                        }
                                        extracted = true;
                                        break;
                                    }
                                }
                                if(!extracted) {
                                    feedPlatformStep = 2;
                                    slowMode = false;
                                }
                            }
                            break;
                        case 5:
                            shouldClawClose = true;
                            break;
                        case 6:
                            hoverOverNeighbour(targetDirection[0], targetDirection[1]);
                            break;
                        case 7:
                            slowMode = false;
                            hoverOverNeighbour(platformDir[0], platformDir[1]);
                            if(platform != null) {
                                platform.openClaw();
                            }
                            break;
                        case 8:
                            slowMode = true;
                            gotoNeighbour(platformDir[0], platformDir[1]);
                            break;
                        case 9:
                            shouldClawClose = false;
                            break;
                        case 10:
                            if(platform != null) {
                                platform.setHeldStack(inventory[0]);
                                inventory[0] = null;
                            }
                            break;
                        case 11:
                            if(platform != null) {
                                platform.closeClaw();
                            }
                        case 12:
                            slowMode = false;
                            hoverOverNeighbour(platformDir[0], platformDir[1]);
                            break;
                        case 13:
                            gotoHomePosition();
                            break;
                    }
                    if(isDoneInternal()) {
                        feedPlatformStep++;
                        if(feedPlatformStep > 13) {
                            feedPlatformStep = 0;
                        }
                    }
                }
            }
        }
        */
    }

	private void moveClaw() {
		oldClawProgress = clawProgress;
        if(!shouldClawClose && clawProgress > 0F) {
            clawProgress = Math.max(clawProgress - TileEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 0);
        } else if(shouldClawClose && clawProgress < 1F) {
            clawProgress = Math.min(clawProgress + TileEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 1);
        }
	}
	
    
    private boolean isExportUnit(){
    	return(getBlockMetadata() == 1);
    }

    private boolean isImportUnit(){
    	return(getBlockMetadata() == 0);
    }
    
    public IInventory getInventoryForCurrentDirection(){
        TileEntity te = getTileEntityForCurrentDirection();
        if(te instanceof IInventory) return (IInventory)te;
        return null;
    }

    public boolean switchMode(){
    	if(this.state <= STATE_SEARCH_SRC) {
    		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1 - getBlockMetadata(), 3);
    		return(true);
    	} else {
    		return false;
    	}

    	//PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
    }

    @Override
    public void gotoHomePosition(){
        super.gotoHomePosition();
        shouldClawClose = false;
    }

    @Override
    public boolean isDone(){
        //return pickUpPlatformStackStep == 0 && exportHeldItemStep == 0 && feedPlatformStep == 0 && isDoneInternal();
    	return(isDoneInternal());
    }

    private boolean isDoneInternal(){
    	return(super.isDone());
    	/*
        if(super.isDone()) {
            boolean searchDone = feedPlatformStep != 4 || searchedItemStack != null && inventory[0] != null && searchedItemStack.isItemEqual(inventory[0]) && inventory[0].stackSize == searchedItemStack.stackSize;
            return clawProgress == (shouldClawClose ? 1F : 0F) && searchDone;
        } else {
            return false;
        }
       */
    }

    public void pickUpPlatformItem(){
        if(pickUpPlatformStackStep == 0) {
            pickUpPlatformStackStep = 1;
        }
    }

    public boolean pickUpInventoryItem(List<AssemblyRecipe> list){
        if(feedPlatformStep == 0) {
            feedPlatformStep = 1;
        }
        recipeList = list;
        return feedPlatformStep != 2;
    }

    public void exportHeldItem(){
        if(exportHeldItemStep == 0) {
            exportHeldItemStep = 1;
        }
    }

    public ForgeDirection[] getInventoryDirectionForItem(ItemStack searchedItem){
        if(searchedItem != null) {
            for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if(dir != ForgeDirection.UP && dir != ForgeDirection.DOWN) {
                    TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord, zCoord + dir.offsetZ);
                    if(te instanceof IInventory) {
                        int slot = getInventoryStackLocation(searchedItem, (IInventory)te);
                        if(slot >= 0) return new ForgeDirection[]{dir, ForgeDirection.UNKNOWN};
                    }
                }
            }
            if(canMoveToDiagonalNeighbours()) {
                for(ForgeDirection secDir : new ForgeDirection[]{ForgeDirection.WEST, ForgeDirection.EAST}) {
                    for(ForgeDirection primDir : new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH}) {
                        TileEntity te = worldObj.getTileEntity(xCoord + primDir.offsetX + secDir.offsetX, yCoord, zCoord + primDir.offsetZ + secDir.offsetZ);
                        if(te instanceof IInventory) {
                            int slot = getInventoryStackLocation(searchedItem, (IInventory)te);
                            if(slot >= 0) return new ForgeDirection[]{primDir, secDir};
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param searchedItem
     * @param inventory where the item is being tried to be extracted from the top (respects ISidedInventory)
     * @return returns -1 when the item can't be found / accessed, else it returns the slot the requested stack is in.
     */
    public static int getInventoryStackLocation(ItemStack searchedItem, IInventory inventory){
        if(inventory instanceof ISidedInventory) {
            int[] slotsInTop = ((ISidedInventory)inventory).getAccessibleSlotsFromSide(ForgeDirection.UP.ordinal());
            for(int slot : slotsInTop) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if(stack != null && stack.stackSize > 0 && stack.isItemEqual(searchedItem) && ((ISidedInventory)inventory).canExtractItem(slot, stack, ForgeDirection.UP.ordinal())) return slot;
            }
        } else {
            for(int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if(stack != null && stack.stackSize > 0 && stack.isItemEqual(searchedItem)) return slot;
            }
        }
        return -1;
    }

    /**
     * Returns the inventory the IOUnit can point to for the given item. This should only be invoked when you're sure that there is
     * an accessible inventory with the item surrounding this TE.
     * @param searchedItem
     * @return
     */
    public IInventory getInventory(ItemStack searchedItem){
        ForgeDirection[] inventoryDir = getInventoryDirectionForItem(searchedItem);
        return (IInventory)worldObj.getTileEntity(xCoord + inventoryDir[0].offsetX + inventoryDir[1].offsetX, yCoord, zCoord + inventoryDir[0].offsetZ + inventoryDir[1].offsetZ);
    }

    public ForgeDirection[] getExportLocationForItem(ItemStack exportedItem){
        if(exportedItem != null) {
            for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if(dir != ForgeDirection.UP && dir != ForgeDirection.DOWN) {
                    TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord, zCoord + dir.offsetZ);
                    if(te instanceof IInventory) {
                        int slot = getInventoryPlaceLocation(exportedItem, (IInventory)te);
                        if(slot >= 0) return new ForgeDirection[]{dir, ForgeDirection.UNKNOWN};
                    }
                }
            }
            if(canMoveToDiagonalNeighbours()) {
                for(ForgeDirection secDir : new ForgeDirection[]{ForgeDirection.WEST, ForgeDirection.EAST}) {
                    for(ForgeDirection primDir : new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH}) {
                        TileEntity te = worldObj.getTileEntity(xCoord + primDir.offsetX + secDir.offsetX, yCoord, zCoord + primDir.offsetZ + secDir.offsetZ);
                        if(te instanceof IInventory) {
                            int slot = getInventoryPlaceLocation(exportedItem, (IInventory)te);
                            if(slot >= 0) return new ForgeDirection[]{primDir, secDir};
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param exported
     * @param inventory where the item is being tried to be placed in the top (respects ISidedInventory)
     * @return returns -1 when the item can't be placed / accessed
     */
    public static int getInventoryPlaceLocation(ItemStack exportedItem, IInventory inventory){
        if(inventory instanceof ISidedInventory) {
            int[] slotsInTop = ((ISidedInventory)inventory).getAccessibleSlotsFromSide(ForgeDirection.UP.ordinal());
            for(int slot : slotsInTop) {
                if(inventory.isItemValidForSlot(slot, exportedItem)) {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    if(stack == null || stack.isItemEqual(exportedItem) && stack.getMaxStackSize() > stack.stackSize) return slot;
                }
            }
        } else {
            for(int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                if(inventory.isItemValidForSlot(slot, exportedItem)) {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    if(stack == null || stack.isItemEqual(exportedItem) && stack.getMaxStackSize() > stack.stackSize) return slot;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the inventory the IOUnit can point to export the given item. This should only be invoked when you're sure that there is
     * an accessible inventory with the item surrounding this TE.
     * @param exportedItem
     * @return
     */
    public IInventory getExportInventory(ItemStack exportedItem){
        ForgeDirection[] inventoryDir = getExportLocationForItem(exportedItem);
        return (IInventory)worldObj.getTileEntity(xCoord + inventoryDir[0].offsetX + inventoryDir[1].offsetX, yCoord, zCoord + inventoryDir[0].offsetZ + inventoryDir[1].offsetZ);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        clawProgress = tag.getFloat("clawProgress");
        shouldClawClose = tag.getBoolean("clawClosing");
        pickUpPlatformStackStep = tag.getInteger("platformPickStep");
        feedPlatformStep = tag.getInteger("feedPlatformStep");
        exportHeldItemStep = tag.getInteger("exportHeldItemStep");
        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[1];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setFloat("clawProgress", clawProgress);
        tag.setBoolean("clawClosing", shouldClawClose);
        tag.setInteger("platformPickStep", pickUpPlatformStackStep);
        tag.setInteger("feedPlatformStep", feedPlatformStep);
        tag.setInteger("exportHeldItemStep", exportHeldItemStep);
        // Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();
        for(int currentIndex = 0; currentIndex < inventory.length; ++currentIndex) {
            if(inventory[currentIndex] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                inventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tag.setTag("Items", tagList);
    }

    @Override
    public boolean canMoveToDiagonalNeighbours(){
        return true;
    }

}
