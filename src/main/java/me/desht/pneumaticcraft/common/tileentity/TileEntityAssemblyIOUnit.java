package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.*;

import java.util.List;

public class TileEntityAssemblyIOUnit extends TileEntityAssemblyRobot {
    private static final int INVENTORY_SIZE = 1;

    @DescSynced
    public boolean shouldClawClose;
    @DescSynced
    @LazySynced
    public float clawProgress;
    public float oldClawProgress;
    @DescSynced
    private ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE);
    private List<AssemblyRecipe> recipeList;
    private ItemStack searchedItemStack = ItemStack.EMPTY;
    private byte state = 0;
    private byte tickCounter = 0;
    private boolean hasSwitchedThisTick;
    @DescSynced
    private boolean exporting;

    private final static byte SLEEP_TICKS = 50;

    private final static byte STATE_IDLE = 0;
    private final static byte STATE_SEARCH_SRC = 1;
    private final static byte STATE_CLOSECLAW_AFTER_PICKUP = 5;
    private final static byte STATE_RESET_CLOSECLAW_AFTER_PICKUP = 20;
    private final static byte STATE_RESET_GOTO_IDLE = 26;
    private final static byte STATE_MAX = 127;

    @Override
    public void update() {
        super.update();
        hasSwitchedThisTick = false;
        if (getWorld().isRemote) {
            if (!isClawDone()) moveClaw();
        } else {
            slowMode = false;
            switch (state) {
                case STATE_IDLE:
                    break;
                case STATE_SEARCH_SRC:
                    if (findPickupLocation()) state++;
                    break;
                // rise to the right height for target location
                case 2: // for pickup
                case 7: // for drop-off
                case 22: // for reset
                    if (hoverOverTarget()) state++;
                    break;
                // turn and move to target
                case 3: // for pickup
                case 8: // for drop-off
                case 23: // for reset
                    slowMode = true;
                    if (gotoTarget()) state++;
                    break;
                case 4: // pickup item - need to pick up before closeClaw; claw needs to know item size to 'grab' it!
                    if (getItemFromCurrentDirection()) state++;
                    break;
                case STATE_CLOSECLAW_AFTER_PICKUP:
                case STATE_RESET_CLOSECLAW_AFTER_PICKUP:
                    if (closeClaw()) state++;
                    break;
                case 6:
                case 21:
                    if (findDropOffLocation()) state++;
                    break;
                case 9:
                case 24:
                    if (openClaw()) state++;
                    break;
                case 10: // drop off item
                case 25:
                    if (putItemToCurrentDirection()) state++;
                    break;
                case 11:
                case STATE_RESET_GOTO_IDLE:
                    if (gotoIdlePos()) state = 0;
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
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public boolean reset() {
        if (state >= STATE_RESET_CLOSECLAW_AFTER_PICKUP) {
            return false;
        } else if (!inventory.getStackInSlot(0).isEmpty()) {
            state = STATE_RESET_CLOSECLAW_AFTER_PICKUP;
            return false;
        } else if (state == STATE_IDLE) {
            return true;
        } else {
            state = STATE_RESET_GOTO_IDLE;
            return isIdle();
        }
    }

    /**
     * @return true if the controller should use air and display 'running'
     */
    public boolean pickupItem(List<AssemblyRecipe> list) {
        recipeList = list;

        if (state == STATE_IDLE) state++;

        return state > STATE_IDLE && !isSleeping() // will not use air while waiting for item/inventory to be available
                && state < STATE_MAX;
    }

    private boolean gotoIdlePos() {
        gotoHomePosition();
        return isDoneInternal();
    }

    private boolean findPickupLocation() {
        if (shouldSleep()) return false;

        EnumFacing[] inventoryDir = null;

        if (isImportUnit()) {
            searchedItemStack = ItemStack.EMPTY;
            if (recipeList != null) {
                for (AssemblyRecipe recipe : recipeList) {
                    inventoryDir = getInventoryDirectionForItem(recipe.getInput());
                    if (inventoryDir != null) {
                        searchedItemStack = recipe.getInput();
                        break;
                    }
                }
            }
        } else {
            inventoryDir = getPlatformDirection();
        }

        targetDirection = inventoryDir;

        if (targetDirection == null) {
            sleepBeforeNextSearch();

            return false;
        } else return true;
    }

    private boolean isSleeping() {
        return tickCounter > 0;
    }

    private boolean shouldSleep() {
        if (tickCounter > 0 && tickCounter++ < SLEEP_TICKS) {
            return true;
        } else {
            tickCounter = 0;
            return false;
        }
    }

    private void sleepBeforeNextSearch() {
        tickCounter = 1;
    }

    private boolean findDropOffLocation() {
        if (shouldSleep()) {
            return false;
        }
        targetDirection = isImportUnit() ? getPlatformDirection() : getExportLocationForItem(inventory.getStackInSlot(0));
        if (targetDirection == null) {
            sleepBeforeNextSearch();
            return false;
        } else {
            return true;
        }
    }

    private boolean getItemFromCurrentDirection() {
        TileEntity tile = getTileEntityForCurrentDirection();

        boolean extracted = false;

        /*
         * we must not .reset here because we might inadvertently change this.state right before this.state++
         *
        if((tile == null) || !(tile instanceof IInventory)) // TE / inventory is gone
        	reset();
        */

        if (isImportUnit()) {
            if (searchedItemStack.isEmpty()) { // we don't know what we're supposed to pick up
                reset();
            } else if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler otherInv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                ItemStack currentStack = inventory.getStackInSlot(0);
                int oldStackSize = currentStack.getCount();

                for (int i = 0; i < otherInv.getSlots(); i++) {
                    if (!otherInv.getStackInSlot(i).isEmpty()) {
                        if (currentStack.isEmpty()) {
                            if (otherInv.getStackInSlot(i).isItemEqual(searchedItemStack)) {
                                ItemStack exStack = otherInv.extractItem(i, 1, false);
                                inventory.insertItem(0, exStack, false);
                            }
                        } else if (ItemHandlerHelper.canItemStacksStack(currentStack, otherInv.getStackInSlot(i))) {
                            ItemStack exStack = otherInv.extractItem(i, 1, false);
                            inventory.insertItem(0, exStack, false);
                        }
                        extracted = inventory.getStackInSlot(0).getCount() >= searchedItemStack.getCount();
                        if (extracted) {
                            break;
                        }
                    }
                }

                if (oldStackSize == (inventory.getStackInSlot(0).getCount())) { // nothing picked up, search for different inventory
                    state = STATE_SEARCH_SRC;
                }
            } else {
                state = STATE_SEARCH_SRC; // inventory gone
            }
        } else {
            if (tile instanceof TileEntityAssemblyPlatform) {
                TileEntityAssemblyPlatform plat = (TileEntityAssemblyPlatform) tile;

                if (plat.openClaw()) {
                    inventory.setStackInSlot(0, plat.getHeldStack());
                    plat.setHeldStack(ItemStack.EMPTY);
                    extracted = !inventory.getStackInSlot(0).isEmpty();
                    if (!extracted) { // something went wrong - either the platform is gone altogether, or the item is not there anymore
                        state = STATE_SEARCH_SRC;
                    }
                }
            }
        }

        return extracted;
    }

    private boolean putItemToCurrentDirection() {
        if (isImportUnit()) {
            TileEntity tile = getTileEntityForCurrentDirection();
            if (tile instanceof TileEntityAssemblyPlatform) {
                TileEntityAssemblyPlatform plat = (TileEntityAssemblyPlatform) tile;

                if (inventory.getStackInSlot(0).isEmpty()) {
                    return plat.closeClaw();
                }

                if (plat.isIdle()) {
                    plat.setHeldStack(inventory.getStackInSlot(0));
                    inventory.setStackInSlot(0, ItemStack.EMPTY);
                    return plat.closeClaw();
                }
            } else {
                repeatDropOffSearch(); // platform gone; close claw and search new drop-off-location
            }
        } else {
            TileEntity te = getTileEntityForCurrentDirection();
            if (te == null) repeatDropOffSearch(); // inventory gone; close claw and search new drop-off-location
            else {
                ItemStack currentStack = inventory.getStackInSlot(0);
                int startSize = currentStack.getCount();
                for (int i = 0; i < 6; i++) {
                    ItemStack excess = PneumaticCraftUtils.exportStackToInventory(te, currentStack, EnumFacing.getFront(i));
                    inventory.setStackInSlot(0, excess);
                    if (excess.isEmpty()) break;
                }
                if (currentStack.isEmpty() || startSize != currentStack.getCount())
                    sendDescriptionPacket(); // TODO - is this still needed? Shouldn't @DescSynced on inventory take care of this?

                if (!currentStack.isEmpty() && startSize == currentStack.getCount())
                    repeatDropOffSearch(); // target-inventory full or unavailable
            }

            return inventory.getStackInSlot(0).isEmpty();
        }

        return false;
    }

    private void repeatDropOffSearch() {
        state = state >= STATE_RESET_CLOSECLAW_AFTER_PICKUP ? STATE_RESET_CLOSECLAW_AFTER_PICKUP : STATE_CLOSECLAW_AFTER_PICKUP;
    }

    private boolean closeClaw() {
        shouldClawClose = true;
        return moveClaw();
    }

    private boolean openClaw() {
        shouldClawClose = false;
        return moveClaw();
    }

    private boolean moveClaw() {
        oldClawProgress = clawProgress;

        if (!shouldClawClose && clawProgress > 0F) {
            clawProgress = Math.max(clawProgress - TileEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 0);
        } else if (shouldClawClose && clawProgress < 1F) {
            clawProgress = Math.min(clawProgress + TileEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 1);
        }

        return isClawDone();
    }

    private boolean isClawDone() {
        // need to make sure that clawProgress and oldClawProgress are the same, or we will get rendering artifacts
        return clawProgress == oldClawProgress && clawProgress == (shouldClawClose ? 1F : 0F);
    }

    public boolean isImportUnit() {
        return !exporting;
    }

    public boolean switchMode() {
        if (state <= STATE_SEARCH_SRC) {
            if (!hasSwitchedThisTick) {
                exporting = !exporting;
                hasSwitchedThisTick = true;
            }
            return true;
        } else {
            return false;
        }

        //PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
    }

    @Override
    public void gotoHomePosition() {
        super.gotoHomePosition();

        if (isClawDone()) openClaw();
    }

    @Override
    public boolean isIdle() {
        return state == STATE_IDLE;
    }

    private boolean isDoneInternal() {
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

    private EnumFacing[] getInventoryDirectionForItem(ItemStack searchedItem) {
        ItemStack stack = inventory.getStackInSlot(0);
        if (!searchedItem.isEmpty() && (stack.isEmpty() || stack.isItemEqual(searchedItem))) {
            for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                IItemHandler handler = IOHelper.getInventoryForTE(IOHelper.getNeighbor(this, dir), EnumFacing.UP);
                if (handler != null && !IOHelper.extract(handler, searchedItem, true, true).isEmpty()) {
                    return new EnumFacing[]{dir, null};
                }
            }
            if (canMoveToDiagonalNeighbours()) {
                for (EnumFacing secDir : new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST}) {
                    for (EnumFacing primDir : new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH}) {
                        TileEntity te = getWorld().getTileEntity(getPos().offset(primDir).offset(secDir));
                        IItemHandler handler = IOHelper.getInventoryForTE(te, EnumFacing.UP);
                        if (!IOHelper.extract(handler, searchedItem, true, true).isEmpty()) {
                            return new EnumFacing[]{primDir, secDir};
                        }
                    }
                }
            }
        }
        return null;
    }

    private EnumFacing[] getExportLocationForItem(ItemStack exportedItem) {
        if (!exportedItem.isEmpty()) {
            for (EnumFacing dir : EnumFacing.VALUES) {
                if (dir != EnumFacing.UP && dir != EnumFacing.DOWN) {
                    TileEntity te = getWorld().getTileEntity(getPos().offset(dir));
                    int slot = getInventoryPlaceLocation(exportedItem, te);
                    if (slot >= 0) return new EnumFacing[]{dir, null};
                }
            }
            if (canMoveToDiagonalNeighbours()) {
                for (EnumFacing secDir : new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST}) {
                    for (EnumFacing primDir : new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH}) {
                        TileEntity te = getWorld().getTileEntity(getPos().offset(primDir).offset(secDir));
                        int slot = getInventoryPlaceLocation(exportedItem, te);
                        if (slot >= 0) return new EnumFacing[]{primDir, secDir};
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param exportedItem item to export
     * @param te where the item is being tried to be placed in the top (respects ISidedInventory)
     * @return returns -1 when the item can't be placed / accessed
     */
    private static int getInventoryPlaceLocation(ItemStack exportedItem, TileEntity te) {
        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack excess = handler.insertItem(slot, exportedItem, true);
                if (excess.getCount() < exportedItem.getCount()) {
                    return slot;
                }
            }
        }
        return -1;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        clawProgress = tag.getFloat("clawProgress");
        shouldClawClose = tag.getBoolean("clawClosing");
        state = tag.getByte("state");
        exporting = tag.getBoolean("exporting");
        inventory = new ItemStackHandler(INVENTORY_SIZE);
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setFloat("clawProgress", clawProgress);
        tag.setBoolean("clawClosing", shouldClawClose);
        tag.setByte("state", state);
        tag.setBoolean("exporting", exporting);
        tag.setTag("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public boolean canMoveToDiagonalNeighbours() {
        return true;
    }

}
