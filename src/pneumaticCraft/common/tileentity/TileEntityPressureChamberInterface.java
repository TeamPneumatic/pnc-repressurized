package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.FilteredSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.network.LazySynced;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Sounds;

public class TileEntityPressureChamberInterface extends TileEntityPressureChamberWall implements ISidedInventory,
        IGUITextFieldSensitive, IRedstoneControlled{
    @DescSynced
    @FilteredSynced(index = 0)
    private ItemStack[] inventory = new ItemStack[14];
    @DescSynced
    @LazySynced
    public int inputProgress;
    public int oldInputProgress;
    @DescSynced
    @LazySynced
    public int outputProgress;
    public int oldOutputProgress;
    public static final int MAX_PROGRESS = 40;
    private static final int UPGRADE_SLOT_START = 1;
    private static final int UPGRADE_SLOT_END = 4;
    @GuiSynced
    public EnumInterfaceMode interfaceMode = EnumInterfaceMode.NONE;
    @GuiSynced
    private boolean enoughAir = true;
    @DescSynced
    public EnumFilterMode filterMode = EnumFilterMode.ITEM;
    @GuiSynced
    public int creativeTabID;
    @DescSynced
    public String itemNameFilter = "";
    private boolean isOpeningI;//used to determine sounds.
    private boolean isOpeningO;//used to determine sounds.
    @DescSynced
    private boolean shouldOpenInput, shouldOpenOutput;
    @GuiSynced
    public int redstoneMode;
    private int inputTimeOut;
    private int oldItemCount;

    public enum EnumInterfaceMode{
        NONE, IMPORT, EXPORT;
    }

    public enum EnumFilterMode{
        ITEM, CREATIVE_TAB, NAME_BEGINS, NAME_CONTAINS;
    }

    public TileEntityPressureChamberInterface(){
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 2, 3, UPGRADE_SLOT_END});
    }

    @Override
    public void updateEntity(){
        super.updateEntity();

        boolean wasOpeningI = isOpeningI;
        boolean wasOpeningO = isOpeningO;
        oldInputProgress = inputProgress;
        oldOutputProgress = outputProgress;
        TileEntityPressureChamberValve core = getCore();

        if(!worldObj.isRemote) {
            int itemCount = inventory[0] != null ? inventory[0].stackSize : 0;
            if(oldItemCount != itemCount) {
                oldItemCount = itemCount;
                inputTimeOut = 0;
            }

            interfaceMode = getInterfaceMode(core);
            enoughAir = true;

            if(interfaceMode != EnumInterfaceMode.NONE) {
                if(inventory[0] != null && ++inputTimeOut > 10) {
                    shouldOpenInput = false;
                    if(inputProgress == 0) {
                        shouldOpenOutput = true;
                        if(outputProgress == MAX_PROGRESS) {
                            if(interfaceMode == EnumInterfaceMode.IMPORT) {
                                outputInChamber();
                            } else {
                                exportToInventory();
                            }
                        }
                    }
                } else {
                    shouldOpenOutput = false;
                    if(outputProgress == 0) {
                        shouldOpenInput = true;
                        if(interfaceMode == EnumInterfaceMode.EXPORT && inputProgress == MAX_PROGRESS && redstoneAllows()) {
                            importFromChamber(core);
                        }
                    }
                }
            } else {
                shouldOpenInput = false;
                shouldOpenOutput = false;
            }
        }

        int speed = (int)getSpeedMultiplierFromUpgrades(getUpgradeSlots());

        if(shouldOpenInput) {
            inputProgress = Math.min(inputProgress + speed, MAX_PROGRESS);
            isOpeningI = true;
        } else {
            inputProgress = Math.max(inputProgress - speed, 0);
            isOpeningI = false;
        }

        if(shouldOpenOutput) {
            outputProgress = Math.min(outputProgress + speed, MAX_PROGRESS);
            isOpeningO = true;
        } else {
            outputProgress = Math.max(outputProgress - speed, 0);
            isOpeningO = false;
        }

        if(worldObj.isRemote && (wasOpeningI != isOpeningI || wasOpeningO != isOpeningO)) {
            worldObj.playSound(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, Sounds.INTERFACE_DOOR, 0.1F, 1.0F, true);
        }
    }

    private void exportToInventory(){
        ForgeDirection facing = ForgeDirection.getOrientation(getBlockMetadata());
        TileEntity te = worldObj.getTileEntity(xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ);
        if(te != null) {
            ForgeDirection side = facing.getOpposite();
            ItemStack leftoverStack = PneumaticCraftUtils.exportStackToInventory(te, inventory[0], side);
            if(leftoverStack == null || leftoverStack.stackSize == 0) {
                inventory[0] = null;
            }
        }
    }

    private void importFromChamber(TileEntityPressureChamberValve core){
        ItemStack[] chamberStacks = core.getStacksInChamber();
        for(ItemStack chamberStack : chamberStacks) {
            if((inventory[0] == null || inventory[0].isItemEqual(chamberStack)) && isItemValidForSlot(0, chamberStack)) {
                int maxAllowedItems = Math.abs(core.currentAir) / PneumaticValues.USAGE_CHAMBER_INTERFACE;
                if(maxAllowedItems > 0) {
                    if(inventory[0] != null) maxAllowedItems = Math.min(maxAllowedItems, chamberStack.getMaxStackSize() - inventory[0].stackSize);
                    int transferedItems = Math.min(chamberStack.stackSize, maxAllowedItems);
                    core.addAir((core.currentAir > 0 ? -1 : 1) * transferedItems * PneumaticValues.USAGE_CHAMBER_INTERFACE, ForgeDirection.UNKNOWN);
                    ItemStack transferedStack = chamberStack.copy().splitStack(transferedItems);
                    ItemStack insertedStack = transferedStack.copy();
                    if(inventory[0] != null) insertedStack.stackSize += inventory[0].stackSize;
                    setInventorySlotContents(0, insertedStack);
                    core.clearStacksInChamber(transferedStack);
                }
            }
        }
    }

    private void outputInChamber(){
        TileEntityPressureChamberValve valve = getCore();
        if(valve != null) {
            for(int i = 0; i < 6; i++) {
                int x = xCoord + Facing.offsetsXForSide[i];
                int y = yCoord + Facing.offsetsYForSide[i];
                int z = zCoord + Facing.offsetsZForSide[i];
                if(valve.isCoordWithinChamber(worldObj, x, y, z)) {
                    enoughAir = Math.abs(valve.currentAir) > inventory[0].stackSize * PneumaticValues.USAGE_CHAMBER_INTERFACE;
                    if(enoughAir) {
                        valve.addAir((valve.currentAir > 0 ? -1 : 1) * inventory[0].stackSize * PneumaticValues.USAGE_CHAMBER_INTERFACE, ForgeDirection.UNKNOWN);
                        EntityItem item = new EntityItem(worldObj, x + 0.5D, y + 0.5D, z + 0.5D, inventory[0].copy());
                        worldObj.spawnEntityInWorld(item);
                        setInventorySlotContents(0, null);
                        break;
                    }
                }
            }
        }
    }

    /*
     * public void setCore(TileEntityPressureChamberValve core){ boolean
     * wasNotNull = teValve != null; super.setCore(core); if(worldObj.isRemote)
     * return; if(core != null) modeNeedsChecking = true; else if(wasNotNull)
     * modeNeedsChecking = true; }
     */
    // figure out whether the Interface is exporting or importing.
    private EnumInterfaceMode getInterfaceMode(TileEntityPressureChamberValve core){
        if(core != null) {
            boolean xMid = xCoord != core.multiBlockX && xCoord != core.multiBlockX + core.multiBlockSize - 1;
            boolean yMid = yCoord != core.multiBlockY && yCoord != core.multiBlockY + core.multiBlockSize - 1;
            boolean zMid = zCoord != core.multiBlockZ && zCoord != core.multiBlockZ + core.multiBlockSize - 1;
            int meta = getBlockMetadata();
            if(xMid && yMid && meta == 2 || xMid && zMid && meta == 0 || yMid && zMid && meta == 4) {
                if(xCoord == core.multiBlockX || yCoord == core.multiBlockY || zCoord == core.multiBlockZ) {
                    return EnumInterfaceMode.EXPORT;
                } else {
                    return EnumInterfaceMode.IMPORT;
                }
            } else if(xMid && yMid && meta == 3 || xMid && zMid && meta == 1 || yMid && zMid && meta == 5) {
                if(xCoord == core.multiBlockX || yCoord == core.multiBlockY || zCoord == core.multiBlockZ) {
                    return EnumInterfaceMode.IMPORT;
                } else {
                    return EnumInterfaceMode.EXPORT;
                }
            }
        }
        return EnumInterfaceMode.NONE;
    }

    public List<String> getProblemStat(){
        List<String> textList = new ArrayList<String>();
        if(interfaceMode == EnumInterfaceMode.NONE) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The Interface can't work.", GuiConstants.maxCharPerLineLeft));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70-The Interface is not in a properly formed Pressure Chamber, and/or", GuiConstants.maxCharPerLineLeft));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70-The Interface is not adjacent to an air block of the Pressure Chamber, and/or", GuiConstants.maxCharPerLineLeft));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70-The Interface isn't orientated right.", GuiConstants.maxCharPerLineLeft));
        } else if(!redstoneAllows()) {
            textList.add("gui.tab.problems.redstoneDisallows");
        } else if(!enoughAir) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77There's not enough pressure in the Pressure Chamber to move the items.", GuiConstants.maxCharPerLineLeft));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Apply more pressure to the Pressure Chamber. The required pressure is dependent on the amount of items being transported.", GuiConstants.maxCharPerLineLeft));
        }
        return textList;
    }

    public boolean hasEnoughPressure(){
        return enoughAir;
    }

    @Override
    public boolean redstoneAllows(){
        switch(redstoneMode){
            case 0:
                return true;
            case 1:
                return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
            case 2:
                return !worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
        }
        return false;
    }

    // INVENTORY METHODS- && NBT
    // ------------------------------------------------------------

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);

        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for(int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }

        outputProgress = tag.getInteger("outputProgress");
        inputProgress = tag.getInteger("inputProgress");
        interfaceMode = EnumInterfaceMode.values()[tag.getInteger("interfaceMode")];
        filterMode = EnumFilterMode.values()[tag.getInteger("filterMode")];
        creativeTabID = tag.getInteger("creativeTabID");
        itemNameFilter = tag.getString("itemNameFilter");
        redstoneMode = tag.getInteger("redstoneMode");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);

        // Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();
        for(int i = 0; i < inventory.length; i++)
            if(inventory[i] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)i);
                inventory[i].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        tag.setTag("Items", tagList);
        tag.setInteger("outputProgress", outputProgress);
        tag.setInteger("inputProgress", inputProgress);
        tag.setInteger("interfaceMode", interfaceMode.ordinal());
        tag.setInteger("filterMode", filterMode.ordinal());
        tag.setInteger("creativeTabID", creativeTabID);
        tag.setString("itemNameFilter", itemNameFilter);
        tag.setInteger("redstoneMode", redstoneMode);
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){

        return 14;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot){
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount){
        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            if(itemStack.stackSize <= amount) {
                setInventorySlotContents(slot, null);
            } else {
                itemStack = itemStack.splitStack(amount);
                if(itemStack.stackSize == 0) {
                    setInventorySlotContents(slot, null);
                }
            }
        }

        return itemStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot){
        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            setInventorySlotContents(slot, null);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack){
        inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
        if(!worldObj.isRemote && slot == 0) {
            sendDescriptionPacket();
        }
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public String getInventoryName(){
        return Blockss.pressureChamberInterface.getUnlocalizedName();
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack iStack){
        if(iStack == null) return false;
        switch(filterMode){
            case ITEM:
                boolean filterEmpty = true;
                for(int i = 0; i < 9; i++) {
                    ItemStack filterStack = getStackInSlot(i + 5);
                    if(filterStack != null) {
                        filterEmpty = false;
                        if(iStack.isItemEqual(filterStack)) {
                            return true;
                        }
                    }
                }
                return filterEmpty;
            case CREATIVE_TAB:
                try {
                    int itemCreativeTabIndex = iStack.getItem().getCreativeTab() != null ? iStack.getItem().getCreativeTab().getTabIndex() : -1;
                    if(itemCreativeTabIndex == creativeTabID) {
                        return true;
                    }
                } catch(Throwable e) {//when we are SMP getCreativeTab() is client only.
                    filterMode = EnumFilterMode.NAME_BEGINS;
                }
                return false;
            case NAME_BEGINS:
                return iStack.getDisplayName().toLowerCase().startsWith(itemNameFilter.toLowerCase());
            case NAME_CONTAINS:
                return iStack.getDisplayName().toLowerCase().contains(itemNameFilter.toLowerCase());
        }
        return false;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer){
        return true;
    }

    @Override
    public boolean isGuiUseableByPlayer(EntityPlayer par1EntityPlayer){
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) != this ? false : par1EntityPlayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int var1){
        return new int[]{0};
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, int j){
        return inputProgress == MAX_PROGRESS && j == getRotation().getOpposite().ordinal() && redstoneAllows();
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, int j){
        return outputProgress == MAX_PROGRESS && j == getRotation().ordinal();
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player){
        if(guiID == 1) {
            if(filterMode.ordinal() >= EnumFilterMode.values().length - 1) {
                filterMode = EnumFilterMode.ITEM;
            } else {
                filterMode = EnumFilterMode.values()[filterMode.ordinal() + 1];
            }
            isItemValidForSlot(0, new ItemStack(Items.stick));//when an SideOnly exception is thrown this method automatically will set the filter mode to Item.

        } else if(guiID == 2) {
            creativeTabID++;
            if(creativeTabID == 5 || creativeTabID == 11) creativeTabID++;
            if(creativeTabID >= CreativeTabs.creativeTabArray.length) {
                creativeTabID = 0;
            }
        } else if(guiID == 0) {
            redstoneMode++;
            if(redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public void setText(int textFieldID, String text){
        itemNameFilter = text;
    }

    @Override
    public String getText(int textFieldID){
        return itemNameFilter;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }
}
