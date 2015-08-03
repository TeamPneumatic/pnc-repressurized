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
import pneumaticCraft.common.util.IOHelper;
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
    private List<AssemblyRecipe> recipeList;
    private ItemStack searchedItemStack;
    private byte state = 0;
    private byte tickCounter = 0;
    private boolean hasSwitchedThisTick;

    private final static byte SLEEP_TICKS = 50;

    private final static byte STATE_IDLE = 0;
    private final static byte STATE_SEARCH_SRC = 1;
    private final static byte STATE_CLOSECLAW_AFTER_PICKUP = 5;
    private final static byte STATE_RESET_CLOSECLAW_AFTER_PICKUP = 20;
    private final static byte STATE_RESET_GOTO_IDLE = 26;
    private final static byte STATE_MAX = 127;

    @Override
    public void updateEntity(){
        super.updateEntity();
        hasSwitchedThisTick = false;
        if(worldObj.isRemote) {
            if(!isClawDone()) moveClaw();
        } else {

            slowMode = false;

            switch(state){

                case STATE_IDLE:
                    break;
                case STATE_SEARCH_SRC:
                    if(findPickupLocation()) state++;
                    break;
                // rise to the right height for target location
                case 2: // for pickup
                case 7: // for drop-off
                case 22: // for reset
                    if(hoverOverTarget()) state++;
                    break;
                // turn and move to target
                case 3: // for pickup
                case 8: // for drop-off
                case 23: // for reset
                    slowMode = true;
                    if(gotoTarget()) state++;
                    break;
                case 4: // pickup item - need to pick up before closeClaw; claw needs to know item size to 'grab' it!
                    if(getItemFromCurrentDirection()) state++;
                    break;
                case STATE_CLOSECLAW_AFTER_PICKUP:
                case STATE_RESET_CLOSECLAW_AFTER_PICKUP:
                    if(closeClaw()) state++;
                    break;
                case 6:
                case 21:
                    if(findDropOffLocation()) state++;
                    break;
                case 9:
                case 24:
                    if(openClaw()) state++;
                    break;
                case 10: // drop off item
                case 25:
                    if(putItemToCurrentDirection()) state++;
                    break;
                case 11:
                case STATE_RESET_GOTO_IDLE:
                    if(gotoIdlePos()) state = 0;
                case STATE_MAX: // this will be set if we encounter an unknown state; prevents log-spam that would result from default-case
                    break;
                default:
                    System.out.printf("unexpected state: %d%n", state);
                    state = STATE_MAX;
                    break;
            }
        }
    }

    @Override
    public boolean reset(){
        if(state >= STATE_RESET_CLOSECLAW_AFTER_PICKUP) return false;
        else if(inventory[0] != null) {
            state = STATE_RESET_CLOSECLAW_AFTER_PICKUP;
            return false;
        } else if(state == STATE_IDLE) {
            return true;
        } else {
            state = STATE_RESET_GOTO_IDLE;
            return isIdle();
        }
    }

    /**
     * @return true if the controller should use air and display 'running'
     */
    public boolean pickupItem(List<AssemblyRecipe> list){
        recipeList = list;

        if(state == STATE_IDLE) state++;

        return state > STATE_IDLE && !isSleeping() // will not use air while waiting for item/inventory to be available
                && state < STATE_MAX;
    }

    private boolean gotoIdlePos(){
        gotoHomePosition();
        return isDoneInternal();
    }

    private boolean findPickupLocation(){
        if(shouldSleep()) return false;

        ForgeDirection[] inventoryDir = null;

        if(isImportUnit()) {
            searchedItemStack = null;
            if(recipeList != null) {
                for(AssemblyRecipe recipe : recipeList) {
                    inventoryDir = getInventoryDirectionForItem(recipe.getInput());
                    if(inventoryDir != null) {
                        searchedItemStack = recipe.getInput();
                        break;
                    }
                }
            }
        } else {
            inventoryDir = getPlatformDirection();
        }

        targetDirection = inventoryDir;

        if(targetDirection == null) {
            sleepBeforeNextSearch();

            return false;
        } else return true;
    }

    private boolean isSleeping(){
        return tickCounter > 0;
    }

    private boolean shouldSleep(){
        if(tickCounter > 0 && tickCounter++ < SLEEP_TICKS) {
            return true;
        } else {
            tickCounter = 0;
            return false;
        }
    }

    private void sleepBeforeNextSearch(){
        tickCounter = 1;
    }

    private boolean findDropOffLocation(){
        if(shouldSleep()) return false;

        ForgeDirection[] inventoryDir = null;

        if(isImportUnit()) {
            inventoryDir = getPlatformDirection();
        } else {
            inventoryDir = getExportLocationForItem(inventory[0]);
        }

        targetDirection = inventoryDir;

        if(targetDirection == null) {
            sleepBeforeNextSearch();

            return false;
        } else return true;
    }

    private boolean getItemFromCurrentDirection(){
        TileEntity tile = getTileEntityForCurrentDirection();

        boolean extracted = false;

        /*
         * we must not .reset here because we might inadvertently change this.state right before this.state++
         *
        if((tile == null) || !(tile instanceof IInventory)) // TE / inventory is gone
        	reset();
        */

        if(isImportUnit()) {
            if(searchedItemStack == null) { // we don't know what we're supposed to pick up
                reset();
            } else if(tile instanceof IInventory) {
                IInventory inv = (IInventory)tile;

                int oldStackSize = inventory[0] == null ? 0 : inventory[0].stackSize;

                for(int i = 0; i < inv.getSizeInventory(); i++) {
                    if(inv.getStackInSlot(i) != null) {
                        if(inventory[0] == null) {
                            if(inv.getStackInSlot(i).isItemEqual(searchedItemStack)) {
                                inventory[0] = inv.decrStackSize(i, 1);
                            }
                        } else if(inv.getStackInSlot(i).isItemEqual(inventory[0])) {
                            inv.decrStackSize(i, 1);
                            inventory[0].stackSize++;
                        }
                        extracted = (inventory[0] == null ? 0 : inventory[0].stackSize) == searchedItemStack.stackSize; // we might need to pickup more than 1 item
                        if(extracted) break;
                    }
                }

                if(oldStackSize == (inventory[0] == null ? 0 : inventory[0].stackSize)) // nothing picked up, search for different inventory
                state = STATE_SEARCH_SRC;

            } else state = STATE_SEARCH_SRC; // inventory gone
        } else {
            if(tile instanceof TileEntityAssemblyPlatform) {

                TileEntityAssemblyPlatform plat = (TileEntityAssemblyPlatform)tile;

                if(plat.openClaw()) {
                    inventory[0] = plat.getHeldStack();
                    plat.setHeldStack(null);
                    extracted = inventory[0] != null;

                    if(!extracted) // something went wrong - either the platform is gone altogether, or the item is not there anymore
                    state = STATE_SEARCH_SRC;
                }
            }
        }

        return extracted;
    }

    private boolean putItemToCurrentDirection(){
        if(isImportUnit()) {
            TileEntity tile = getTileEntityForCurrentDirection();
            if(tile instanceof TileEntityAssemblyPlatform) {

                TileEntityAssemblyPlatform plat = (TileEntityAssemblyPlatform)tile;

                if(inventory[0] == null) return plat.closeClaw();

                if(plat.isIdle()) {
                    plat.setHeldStack(inventory[0]);
                    inventory[0] = null;
                    return plat.closeClaw();
                }
            } else repeatDropOffSearch(); // platform gone; close claw and search new drop-off-location
        } else {
            IInventory inv = getInventoryForCurrentDirection();
            if(inv == null) repeatDropOffSearch(); // inventory gone; close claw and search new drop-off-location
            else {
                int startSize = inventory[0].stackSize;
                for(int i = 0; i < 6; i++) {
                    inventory[0] = PneumaticCraftUtils.exportStackToInventory(inv, inventory[0], ForgeDirection.getOrientation(i));
                    if(inventory[0] == null) break;
                }
                if(inventory[0] == null || startSize != inventory[0].stackSize) sendDescriptionPacket(); // TODO - is this still needed? Shouldn't @DescSynced on inventory take care of this?

                if(inventory[0] != null && startSize == inventory[0].stackSize) repeatDropOffSearch(); // target-inventory full or unavailable
            }

            return inventory[0] == null;
        }

        return false;
    }

    private void repeatDropOffSearch(){
        state = state >= STATE_RESET_CLOSECLAW_AFTER_PICKUP ? STATE_RESET_CLOSECLAW_AFTER_PICKUP : STATE_CLOSECLAW_AFTER_PICKUP;
    }

    private boolean closeClaw(){
        shouldClawClose = true;
        return moveClaw();
    }

    private boolean openClaw(){
        shouldClawClose = false;
        return moveClaw();
    }

    private boolean moveClaw(){
        oldClawProgress = clawProgress;

        if(!shouldClawClose && clawProgress > 0F) {
            clawProgress = Math.max(clawProgress - TileEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 0);
        } else if(shouldClawClose && clawProgress < 1F) {
            clawProgress = Math.min(clawProgress + TileEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 1);
        }

        return isClawDone();
    }

    private boolean isClawDone(){
        // need to make sure that clawProgress and oldClawProgress are the same, or we will get rendering artifacts
        return clawProgress == oldClawProgress && clawProgress == (shouldClawClose ? 1F : 0F);
    }

    private boolean isImportUnit(){
        return getBlockMetadata() == 0;
    }

    public IInventory getInventoryForCurrentDirection(){
        TileEntity te = getTileEntityForCurrentDirection();
        if(te instanceof IInventory) return (IInventory)te;
        return null;
    }

    public boolean switchMode(){
        if(state <= STATE_SEARCH_SRC) {
            if(!hasSwitchedThisTick) {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1 - getBlockMetadata(), 3);
                hasSwitchedThisTick = true;
            }
            return true;
        } else {
            return false;
        }

        //PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
    }

    @Override
    public void gotoHomePosition(){
        super.gotoHomePosition();

        if(isClawDone()) openClaw();
    }

    @Override
    public boolean isIdle(){
        return state == STATE_IDLE;
    }

    private boolean isDoneInternal(){
        return super.isDoneMoving();
        /*
        if(super.isDone()) {
            boolean searchDone = feedPlatformStep != 4 || searchedItemStack != null && inventory[0] != null && searchedItemStack.isItemEqual(inventory[0]) && inventory[0].stackSize == searchedItemStack.stackSize;
            return clawProgress == (shouldClawClose ? 1F : 0F) && searchDone;
        } else {
            return false;
        }
        */
    }

    public ForgeDirection[] getInventoryDirectionForItem(ItemStack searchedItem){
        if(searchedItem != null && (inventory[0] == null || inventory[0].isItemEqual(searchedItem))) {
            for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if(dir != ForgeDirection.UP && dir != ForgeDirection.DOWN) {
                    TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord, zCoord + dir.offsetZ);
                    if(te instanceof IInventory) {
                        if(IOHelper.extract(te, ForgeDirection.UP, searchedItem, true, true) != null) return new ForgeDirection[]{dir, ForgeDirection.UNKNOWN};
                    }
                }
            }
            if(canMoveToDiagonalNeighbours()) {
                for(ForgeDirection secDir : new ForgeDirection[]{ForgeDirection.WEST, ForgeDirection.EAST}) {
                    for(ForgeDirection primDir : new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH}) {
                        TileEntity te = worldObj.getTileEntity(xCoord + primDir.offsetX + secDir.offsetX, yCoord, zCoord + primDir.offsetZ + secDir.offsetZ);
                        if(te instanceof IInventory) {
                            if(IOHelper.extract(te, ForgeDirection.UP, searchedItem, true, true) != null) return new ForgeDirection[]{primDir, secDir};
                        }
                    }
                }
            }
        }
        return null;
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
        state = tag.getByte("state");
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
        tag.setByte("state", state);
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
