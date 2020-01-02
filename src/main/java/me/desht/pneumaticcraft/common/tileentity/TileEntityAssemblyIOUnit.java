package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.crafting.recipe.IAssemblyRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.handler.RenderedItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Collection;

public class TileEntityAssemblyIOUnit extends TileEntityAssemblyRobot {
    private static final int INVENTORY_SIZE = 1;

    @DescSynced
    private boolean shouldClawClose;
    @DescSynced
    @LazySynced
    public float clawProgress;
    public float oldClawProgress;
    @DescSynced
    private final RenderedItemStackHandler itemHandler = new RenderedItemStackHandler(this);
    private final LazyOptional<IItemHandlerModifiable> inventoryCap = LazyOptional.of(() -> itemHandler);

    private Collection<IAssemblyRecipe> recipeList;
    private ItemStack searchedItemStack = ItemStack.EMPTY;
    private byte state = 0;
    private byte tickCounter = 0;
    private boolean hasSwitchedThisTick;
//    @DescSynced
//    private boolean exporting;

    private final static byte SLEEP_TICKS = 50;

    private final static byte STATE_IDLE = 0;
    private final static byte STATE_SEARCH_SRC = 1;
    private final static byte STATE_CLOSECLAW_AFTER_PICKUP = 5;
    private final static byte STATE_RESET_CLOSECLAW_AFTER_PICKUP = 20;
    private final static byte STATE_RESET_GOTO_IDLE = 26;
    private final static byte STATE_MAX = 127;

    public TileEntityAssemblyIOUnit() {
        super(ModTileEntityTypes.ASSEMBLY_IO_UNIT);
    }

    @Override
    public void tick() {
        super.tick();
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
    protected LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return inventoryCap;
    }

    @Override
    public boolean reset() {
        if (state >= STATE_RESET_CLOSECLAW_AFTER_PICKUP) {
            return false;
        } else if (!itemHandler.getStackInSlot(0).isEmpty()) {
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
    public boolean pickupItem(Collection<IAssemblyRecipe> list) {
        recipeList = list;

        if (state == STATE_IDLE) state++;

        // don't use air while waiting for item/inventory to be available
        return state > STATE_IDLE && !isSleeping() && state < STATE_MAX;
    }

    private boolean gotoIdlePos() {
        gotoHomePosition();
        return isDoneMoving();
    }

    private boolean findPickupLocation() {
        if (shouldSleep()) return false;

        Direction[] inventoryDir = null;

        if (isImportUnit()) {
            searchedItemStack = ItemStack.EMPTY;
            if (recipeList != null) {
                for (IAssemblyRecipe recipe : recipeList) {
                    ItemImport result = getInventoryDirectionForItem(recipe);
                    if (result != null) {
                        searchedItemStack = result.stack;
                        inventoryDir = result.dirs;
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
        targetDirection = isImportUnit() ? getPlatformDirection() : getExportLocationForItem(itemHandler.getStackInSlot(0));
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

        if (isImportUnit()) {
            if (searchedItemStack.isEmpty()) { // we don't know what we're supposed to pick up
                reset();
            } else {
                extracted = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).map(otherInv -> {
                    ItemStack currentStack = itemHandler.getStackInSlot(0);
                    int oldStackSize = currentStack.getCount();

                    boolean ret = false;
                    for (int i = 0; i < otherInv.getSlots(); i++) {
                        if (!otherInv.getStackInSlot(i).isEmpty()) {
                            if (currentStack.isEmpty()) {
                                if (otherInv.getStackInSlot(i).isItemEqual(searchedItemStack)) {
                                    ItemStack exStack = otherInv.extractItem(i, 1, false);
                                    itemHandler.insertItem(0, exStack, false);
                                }
                            } else if (ItemHandlerHelper.canItemStacksStack(currentStack, otherInv.getStackInSlot(i))) {
                                ItemStack exStack = otherInv.extractItem(i, 1, false);
                                itemHandler.insertItem(0, exStack, false);
                            }
                            ret = itemHandler.getStackInSlot(0).getCount() >= searchedItemStack.getCount();
                            if (ret) {
                                break;
                            }
                        }
                    }

                    if (oldStackSize == (itemHandler.getStackInSlot(0).getCount())) { // nothing picked up, search for different inventory
                        state = STATE_SEARCH_SRC;
                    }
                    return ret;
                }).orElseGet(() -> {
                    state = STATE_SEARCH_SRC; // inventory gone
                    return false;
                });
            }
        } else {
            if (tile instanceof TileEntityAssemblyPlatform) {
                TileEntityAssemblyPlatform plat = (TileEntityAssemblyPlatform) tile;

                if (plat.openClaw()) {
                    itemHandler.setStackInSlot(0, plat.getHeldStack());
                    plat.setHeldStack(ItemStack.EMPTY);
                    extracted = !itemHandler.getStackInSlot(0).isEmpty();
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

                if (itemHandler.getStackInSlot(0).isEmpty()) {
                    return plat.closeClaw();
                }

                if (plat.isIdle()) {
                    plat.setHeldStack(itemHandler.getStackInSlot(0));
                    itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                    return plat.closeClaw();
                }
            } else {
                repeatDropOffSearch(); // platform gone; close claw and search new drop-off-location
            }
        } else {
            TileEntity te = getTileEntityForCurrentDirection();
            if (te == null) repeatDropOffSearch(); // inventory gone; close claw and search new drop-off-location
            else {
                ItemStack currentStack = itemHandler.getStackInSlot(0);
                int startSize = currentStack.getCount();
                for (int i = 0; i < 6; i++) {
                    ItemStack excess = IOHelper.insert(te, currentStack, Direction.byIndex(i), false);
                    itemHandler.setStackInSlot(0, excess);
                    if (excess.isEmpty()) break;
                }
                currentStack = itemHandler.getStackInSlot(0);
                if (currentStack.isEmpty() || startSize != currentStack.getCount())
                    sendDescriptionPacket(); // TODO - is this still needed? Shouldn't @DescSynced on inventory take care of this?

                if (!currentStack.isEmpty() && startSize == currentStack.getCount())
                    repeatDropOffSearch(); // target-inventory full or unavailable
            }

            return itemHandler.getStackInSlot(0).isEmpty();
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
        markDirty();
        return isClawDone();
    }

    private boolean isClawDone() {
        // need to make sure that clawProgress and oldClawProgress are the same, or we will get rendering artifacts
        return clawProgress == oldClawProgress && clawProgress == (shouldClawClose ? 1F : 0F);
    }

    public boolean isImportUnit() {
        return getBlockState().getBlock() == ModBlocks.ASSEMBLY_IO_UNIT_IMPORT;
    }

    public void switchMode() {
        if (state <= STATE_SEARCH_SRC) {
            if (!hasSwitchedThisTick) {
                hasSwitchedThisTick = true;
                markDirty();
                invalidateSystem();
            }
        }
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

    @Override
    public AssemblyProgram.EnumMachine getAssemblyType() {
        return isImportUnit() ? AssemblyProgram.EnumMachine.IO_UNIT_IMPORT : AssemblyProgram.EnumMachine.IO_UNIT_EXPORT;
    }

    private ItemImport getInventoryDirectionForItem(IAssemblyRecipe recipe) {
        ItemStack heldStack = itemHandler.getStackInSlot(0);
        if (heldStack.isEmpty() || recipe.getInput().test(heldStack)) {
            for (Direction dir : PneumaticCraftUtils.HORIZONTALS) {
                ItemStack found = IOHelper.getInventoryForTE(getCachedNeighbor(dir), Direction.UP)
                        .map(h -> findIngredientInInventory(h, recipe)).orElse(ItemStack.EMPTY);
                if (!found.isEmpty()) return new ItemImport(new Direction[]{dir, null}, found);
            }
            if (canMoveToDiagonalNeighbours()) {
                for (Direction secDir : new Direction[]{Direction.WEST, Direction.EAST}) {
                    for (Direction primDir : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
                        TileEntity te = getWorld().getTileEntity(getPos().offset(primDir).offset(secDir));
                        ItemStack found = IOHelper.getInventoryForTE(te, Direction.UP)
                                .map(h -> findIngredientInInventory(h, recipe)).orElse(ItemStack.EMPTY);
                        if (!found.isEmpty()) return new ItemImport(new Direction[]{primDir, secDir}, found);
                    }
                }
            }
        }

        return null;
    }

    private ItemStack findIngredientInInventory(IItemHandler handler, IAssemblyRecipe recipe) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getCount() >= recipe.getInputAmount() && recipe.getInput().test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static class ItemImport {
        final Direction[] dirs;
        final ItemStack stack;

        ItemImport(Direction[] dirs, ItemStack stack) {
            this.dirs = dirs;
            this.stack = stack;
        }
    }

    private Direction[] getExportLocationForItem(ItemStack exportedItem) {
        if (!exportedItem.isEmpty()) {
            for (Direction dir : PneumaticCraftUtils.HORIZONTALS) {
                TileEntity te = getWorld().getTileEntity(getPos().offset(dir));
                int slot = getPlacementSlot(exportedItem, te);
                if (slot >= 0) return new Direction[]{dir, null};
            }
            if (canMoveToDiagonalNeighbours()) {
                for (Direction secDir : new Direction[]{Direction.WEST, Direction.EAST}) {
                    for (Direction primDir : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
                        TileEntity te = getWorld().getTileEntity(getPos().offset(primDir).offset(secDir));
                        int slot = getPlacementSlot(exportedItem, te);
                        if (slot >= 0) return new Direction[]{primDir, secDir};
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find a slot into which to place an exported item.  Note that other assembly robots are not valid export
     * locations, but any other TE which provides CAPABILITY_ITEM_HANDLER on the top face is a valid candidate.
     *
     * @param exportedItem item to export
     * @param te where the item is being attempted to insert to (will use the top face for IItemHandler cap.)
     * @return the placement slot, or -1 when the item can't be placed / accessed
     */
    private static int getPlacementSlot(ItemStack exportedItem, TileEntity te) {
        if (te == null || te instanceof TileEntityAssemblyRobot) return -1;

        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(handler -> {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack excess = handler.insertItem(slot, exportedItem, true);
                if (excess.getCount() < exportedItem.getCount()) {
                    return slot;
                }
            }
            return -1;
        }).orElse(-1);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        clawProgress = tag.getFloat("clawProgress");
        shouldClawClose = tag.getBoolean("clawClosing");
        state = tag.getByte("state");
        itemHandler.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return itemHandler;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putFloat("clawProgress", clawProgress);
        tag.putBoolean("clawClosing", shouldClawClose);
        tag.putByte("state", state);
        tag.put("Items", itemHandler.serializeNBT());
        return tag;
    }

    @Override
    public boolean canMoveToDiagonalNeighbours() {
        return true;
    }
}
