package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class TileEntityPressureChamberInterface extends TileEntityPressureChamberWall implements IGUITextFieldSensitive, IRedstoneControlled {
    public static final int MAX_PROGRESS = 40;
    public static final int INVENTORY_SIZE = 1;
    private static final int FILTER_SIZE = 9;

    @DescSynced
    private final PressureChamberInterfaceHandler inventory = new PressureChamberInterfaceHandler();
    @DescSynced
    @LazySynced
    public int inputProgress;
    @DescSynced
    @LazySynced
    public int outputProgress;
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
    private boolean isOpeningI; // used to determine sounds.
    private boolean isOpeningO; // used to determine sounds.
    @DescSynced
    private boolean shouldOpenInput, shouldOpenOutput;
    @GuiSynced
    public int redstoneMode;
    private int inputTimeOut;
    private int oldItemCount;
    private final PressureChamberFilterHandler filterHandler = new PressureChamberFilterHandler();

    public enum EnumInterfaceMode {
        NONE, IMPORT, EXPORT
    }

    public enum EnumFilterMode {
        ITEM, CREATIVE_TAB, NAME_BEGINS, NAME_CONTAINS
    }

    public TileEntityPressureChamberInterface() {
        super(4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public void update() {
        super.update();

        boolean wasOpeningI = isOpeningI;
        boolean wasOpeningO = isOpeningO;
        TileEntityPressureChamberValve core = getCore();

        if (!getWorld().isRemote) {
            int itemCount = inventory.getStackInSlot(0).getCount();
            if (oldItemCount != itemCount) {
                oldItemCount = itemCount;
                inputTimeOut = 0;
            }

            interfaceMode = getInterfaceMode(core);
            enoughAir = true;

            if (interfaceMode != EnumInterfaceMode.NONE) {
                if (!inventory.getStackInSlot(0).isEmpty() && ++inputTimeOut > 10) {
                    shouldOpenInput = false;
                    if (inputProgress == 0) {
                        shouldOpenOutput = true;
                        if (outputProgress == MAX_PROGRESS) {
                            if (interfaceMode == EnumInterfaceMode.IMPORT) {
                                outputInChamber();
                            } else {
                                exportToInventory();
                            }
                        }
                    }
                } else {
                    shouldOpenOutput = false;
                    if (outputProgress == 0) {
                        shouldOpenInput = true;
                        if (interfaceMode == EnumInterfaceMode.EXPORT && inputProgress == MAX_PROGRESS && redstoneAllows()) {
                            importFromChamber(core);
                        }
                    }
                }
            } else {
                shouldOpenInput = false;
                shouldOpenOutput = false;
            }
        }

        int speed = (int) getSpeedMultiplierFromUpgrades();

        if (shouldOpenInput) {
            inputProgress = Math.min(inputProgress + speed, MAX_PROGRESS);
            isOpeningI = true;
        } else {
            inputProgress = Math.max(inputProgress - speed, 0);
            isOpeningI = false;
        }

        if (shouldOpenOutput) {
            outputProgress = Math.min(outputProgress + speed, MAX_PROGRESS);
            isOpeningO = true;
        } else {
            outputProgress = Math.max(outputProgress - speed, 0);
            isOpeningO = false;
        }

        if (getWorld().isRemote && (wasOpeningI != isOpeningI || wasOpeningO != isOpeningO)) {
            getWorld().playSound(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, Sounds.INTERFACE_DOOR, SoundCategory.BLOCKS, 0.1F, 1.0F, true);
        }
    }

    private void exportToInventory() {
        EnumFacing facing = getRotation();
        TileEntity te = getWorld().getTileEntity(getPos().offset(facing));
        if (te != null) {
            ItemStack leftoverStack = PneumaticCraftUtils.exportStackToInventory(te, inventory.getStackInSlot(0), facing.getOpposite());
            if (leftoverStack.isEmpty()) {
                inventory.setStackInSlot(0, ItemStack.EMPTY);
            }
        }
    }

    private void importFromChamber(TileEntityPressureChamberValve core) {
        ItemStack[] chamberStacks = core.getStacksInChamber();
        for (ItemStack chamberStack : chamberStacks) {
            ItemStack inputStack = inventory.getStackInSlot(0);
            if ((inputStack.isEmpty() || inputStack.isItemEqual(chamberStack)) && filterHandler.doesItemMatchFilter(chamberStack)) {
                int maxAllowedItems = Math.abs(core.getAirHandler(null).getAir()) / PneumaticValues.USAGE_CHAMBER_INTERFACE;
                if (maxAllowedItems > 0) {
                    if (!inputStack.isEmpty())
                        maxAllowedItems = Math.min(maxAllowedItems, chamberStack.getMaxStackSize() - inputStack.getCount());
                    int transferredItems = Math.min(chamberStack.getCount(), maxAllowedItems);
                    core.addAir((core.getAirHandler(null).getAir() > 0 ? -1 : 1) * transferredItems * PneumaticValues.USAGE_CHAMBER_INTERFACE);
                    ItemStack transferedStack = chamberStack.copy().splitStack(transferredItems);
                    ItemStack insertedStack = transferedStack.copy();
                    insertedStack.grow(inputStack.getCount());
                    inventory.setStackInSlot(0, insertedStack);
                    core.clearStacksInChamber(transferedStack);
                }
            }
        }
    }

    private void outputInChamber() {
        // place items from the interface block into the pressure chamber
        // all items in the interface will be moved at once, but the pressure chamber must have enough presure to do so
        TileEntityPressureChamberValve valve = getCore();
        if (valve != null) {
            for (EnumFacing d : EnumFacing.VALUES) {
                BlockPos neighborPos = getPos().offset(d);
                ItemStack inputStack = inventory.getStackInSlot(0);
                if (valve.isCoordWithinChamber(getWorld(), neighborPos)) {
                    enoughAir = Math.abs(valve.getAirHandler(null).getAir()) > inputStack.getCount() * PneumaticValues.USAGE_CHAMBER_INTERFACE;
                    if (enoughAir) {
                        valve.addAir((valve.getAirHandler(null).getAir() > 0 ? -1 : 1) * inputStack.getCount() * PneumaticValues.USAGE_CHAMBER_INTERFACE);
                        EntityItem item = new EntityItem(getWorld(), neighborPos.getX() + 0.5, neighborPos.getY() + 0.5, neighborPos.getZ() + 0.5D, inputStack.copy());
                        getWorld().spawnEntity(item);
                        inventory.setStackInSlot(0, ItemStack.EMPTY);
                        break;
                    }
                }
            }
        }
    }

    // Figure out whether the Interface is exporting or importing.
    private EnumInterfaceMode getInterfaceMode(TileEntityPressureChamberValve core) {
        if (core != null) {
            boolean xMid = getPos().getX() != core.multiBlockX && getPos().getX() != core.multiBlockX + core.multiBlockSize - 1;
            boolean yMid = getPos().getY() != core.multiBlockY && getPos().getY() != core.multiBlockY + core.multiBlockSize - 1;
            boolean zMid = getPos().getZ() != core.multiBlockZ && getPos().getZ() != core.multiBlockZ + core.multiBlockSize - 1;
            EnumFacing rotation = getRotation();
            if (xMid && yMid && rotation == EnumFacing.NORTH || xMid && zMid && rotation == EnumFacing.DOWN || yMid && zMid && rotation == EnumFacing.WEST) {
                if (getPos().getX() == core.multiBlockX || getPos().getY() == core.multiBlockY || getPos().getZ() == core.multiBlockZ) {
                    return EnumInterfaceMode.EXPORT;
                } else {
                    return EnumInterfaceMode.IMPORT;
                }
            } else if (xMid && yMid && rotation == EnumFacing.SOUTH || xMid && zMid && rotation == EnumFacing.UP || yMid && zMid && rotation == EnumFacing.EAST) {
                if (getPos().getX() == core.multiBlockX || getPos().getY() == core.multiBlockY || getPos().getZ() == core.multiBlockZ) {
                    return EnumInterfaceMode.IMPORT;
                } else {
                    return EnumInterfaceMode.EXPORT;
                }
            }
        }
        return EnumInterfaceMode.NONE;
    }

    public List<String> getProblemStat() {
        List<String> textList = new ArrayList<>();
        if (interfaceMode == EnumInterfaceMode.NONE) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The Interface can't work:"));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70\u2022 The Interface is not in a properly formed Pressure Chamber, and/or"));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70\u2022 The Interface is not adjacent to an air block of the Pressure Chamber, and/or"));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70\u2022 The Interface isn't oriented properly"));
        } else if (!redstoneAllows()) {
            textList.add("gui.tab.problems.redstoneDisallows");
        } else if (!enoughAir) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a7Not enough pressure in the Pressure Chamber to move the items."));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Apply more pressure to the Pressure Chamber. The required pressure is dependent on the amount of items being transported."));
        }
        return textList;
    }

    public boolean hasEnoughPressure() {
        return enoughAir;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        inventory.deserializeNBT(tag.getCompoundTag("Items"));
        filterHandler.deserializeNBT(tag.getCompoundTag("filter"));
        outputProgress = tag.getInteger("outputProgress");
        inputProgress = tag.getInteger("inputProgress");
        interfaceMode = EnumInterfaceMode.values()[tag.getInteger("interfaceMode")];
        filterMode = EnumFilterMode.values()[tag.getInteger("filterMode")];
        creativeTabID = tag.getInteger("creativeTabID");
        itemNameFilter = tag.getString("itemNameFilter");
        redstoneMode = tag.getInteger("redstoneMode");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Items", inventory.serializeNBT());
        tag.setTag("filter", filterHandler.serializeNBT());
        tag.setInteger("outputProgress", outputProgress);
        tag.setInteger("inputProgress", inputProgress);
        tag.setInteger("interfaceMode", interfaceMode.ordinal());
        tag.setInteger("filterMode", filterMode.ordinal());
        tag.setInteger("creativeTabID", creativeTabID);
        tag.setString("itemNameFilter", itemNameFilter);
        tag.setInteger("redstoneMode", redstoneMode);
        return tag;
    }

    @Override
    public String getName() {
        return Blockss.PRESSURE_CHAMBER_INTERFACE.getUnlocalizedName();
    }

    @Override
    public boolean isGuiUseableByPlayer(EntityPlayer player) {
        return getWorld().getTileEntity(getPos()) == this
                && player.getDistanceSq(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player) {
        if (guiID == 1) {
            if (filterMode.ordinal() >= EnumFilterMode.values().length - 1) {
                filterMode = EnumFilterMode.ITEM;
            } else {
                filterMode = EnumFilterMode.values()[filterMode.ordinal() + 1];
            }
            //when an SideOnly exception is thrown this method automatically will set the filter mode to Item.
            filterHandler.doesItemMatchFilter(new ItemStack(Items.STICK));

        } else if (guiID == 2) {
            creativeTabID++;
            if (creativeTabID == 5 || creativeTabID == 11) creativeTabID++;
            if (creativeTabID >= CreativeTabs.CREATIVE_TAB_ARRAY.length) {
                creativeTabID = 0;
            }
        } else if (guiID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public void setText(int textFieldID, String text) {
        itemNameFilter = text;
    }

    @Override
    public String getText(int textFieldID) {
        return itemNameFilter;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    public IItemHandler getFilterHandler() {
        return filterHandler;
    }

    private class PressureChamberFilterHandler extends ItemStackHandler {
        PressureChamberFilterHandler() {
            super(FILTER_SIZE);
        }

        boolean doesItemMatchFilter(ItemStack itemStack) {
            if (itemStack.isEmpty()) return true;

            switch (filterMode) {
                case ITEM:
                    boolean filterEmpty = true;
                    for (int i = 0; i < 9; i++) {
                        ItemStack filterStack = getStackInSlot(i);
                        if (!filterStack.isEmpty()) {
                            filterEmpty = false;
                            if (itemStack.isItemEqual(filterStack)) {
                                return true;
                            }
                        }
                    }
                    return filterEmpty;
                case CREATIVE_TAB:
                    try {
                        int itemCreativeTabIndex = itemStack.getItem().getCreativeTab() != null ? itemStack.getItem().getCreativeTab().getTabIndex() : -1;
                        if (itemCreativeTabIndex == creativeTabID) {
                            return true;
                        }
                    } catch (Throwable e) {
                        //when we are SMP getCreativeTab() is client only.
                        filterMode = EnumFilterMode.NAME_BEGINS;
                    }
                    return false;
                case NAME_BEGINS:
                    return itemStack.getDisplayName().toLowerCase().startsWith(itemNameFilter.toLowerCase());
                case NAME_CONTAINS:
                    return itemStack.getDisplayName().toLowerCase().contains(itemNameFilter.toLowerCase());
            }
            return false;
        }
    }

    private class PressureChamberInterfaceHandler extends ItemStackHandler {
        PressureChamberInterfaceHandler() {
            super(INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if(!getWorld().isRemote && slot == 0) {
                sendDescriptionPacket();
            }
        }

    }
}
