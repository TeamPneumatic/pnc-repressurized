package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerPressureChamberInterface;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityPressureChamberInterface extends TileEntityPressureChamberWall implements ITickableTileEntity, IGUITextFieldSensitive, IRedstoneControlled, INamedContainerProvider {
    public static final int MAX_PROGRESS = 40;
    public static final int INVENTORY_SIZE = 1;
    private static final int FILTER_SIZE = 9;
    private static final int MIN_SOUND_INTERVAL = 500;  // ticks - the sound effect is ~2.5s long

    @DescSynced
    private final PressureChamberInterfaceHandler inventory = new PressureChamberInterfaceHandler();
    private final LazyOptional<IItemHandlerModifiable> invCap = LazyOptional.of(() -> inventory);
    @DescSynced
    @LazySynced
    public int inputProgress;
    public int oldInputProgress;
    @DescSynced
    @LazySynced
    public int outputProgress;
    public int oldOutputProgress;
    @GuiSynced
    public InterfaceDirection interfaceMode = InterfaceDirection.NONE;
    @GuiSynced
    private boolean enoughAir = true;
    @DescSynced
    public FilterMode filterMode = FilterMode.ITEM;
    @DescSynced
    public String itemNameFilter = "";
    private boolean isOpeningI; // used to determine sounds.
    private boolean isOpeningO; // used to determine sounds.
    private int soundTimer;
    @DescSynced
    private boolean shouldOpenInput, shouldOpenOutput;
    @GuiSynced
    public int redstoneMode;
    private int inputTimeOut;
    private int oldItemCount;
    private final PressureChamberFilterHandler filterHandler = new PressureChamberFilterHandler();

    public enum InterfaceDirection {
        NONE, IMPORT, EXPORT
    }

    public enum FilterMode {
        ITEM, NAME_BEGINS, NAME_CONTAINS
    }

    public TileEntityPressureChamberInterface() {
        super(ModTileEntityTypes.PRESSURE_CHAMBER_INTERFACE, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerPressureChamberInterface(i, playerInventory, getPos());
    }

    @Override
    public void tick() {
        tickImpl();

        boolean wasOpeningI = isOpeningI;
        boolean wasOpeningO = isOpeningO;
        oldInputProgress = inputProgress;
        oldOutputProgress = outputProgress;
        TileEntityPressureChamberValve core = getCore();

        if (!getWorld().isRemote) {
            int itemCount = inventory.getStackInSlot(0).getCount();
            if (oldItemCount != itemCount) {
                oldItemCount = itemCount;
                inputTimeOut = 0;
            }

            interfaceMode = getInterfaceMode(core);
            enoughAir = true;

            if (interfaceMode != InterfaceDirection.NONE) {
                if (!inventory.getStackInSlot(0).isEmpty() && ++inputTimeOut > 10) {
                    shouldOpenInput = false;
                    if (inputProgress == 0) {
                        shouldOpenOutput = true;
                        if (outputProgress == MAX_PROGRESS) {
                            if (interfaceMode == InterfaceDirection.IMPORT) {
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
                        if (interfaceMode == InterfaceDirection.EXPORT && inputProgress == MAX_PROGRESS && redstoneAllows()) {
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

        if (getWorld().isRemote && soundTimer++ >= MIN_SOUND_INTERVAL && (wasOpeningI != isOpeningI || wasOpeningO != isOpeningO)) {
            getWorld().playSound(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, ModSounds.INTERFACE_DOOR, SoundCategory.BLOCKS, 0.5F, 1.0F, true);
            soundTimer = 0;
        }
    }

    public ItemStack getStackInInterface() {
        return inventory.getStackInSlot(0);
    }

    private void exportToInventory() {
        Direction facing = getRotation();
        TileEntity te = getCachedNeighbor(facing);
        if (te != null) {
            ItemStack stack = inventory.getStackInSlot(0);
            int count = stack.getCount();
            ItemStack leftoverStack = IOHelper.insert(te, stack.copy(), facing.getOpposite(), false);
            stack.shrink(count - leftoverStack.getCount());
        }
    }

    private void importFromChamber(TileEntityPressureChamberValve core) {
        ItemStackHandler chamberStacks = core.getStacksInChamber();
        for (int i = 0; i < chamberStacks.getSlots(); i++) {
            ItemStack chamberStack = chamberStacks.getStackInSlot(i);
            if (chamberStack.isEmpty()) {
                continue;
            }
            ItemStack inputStack = inventory.getStackInSlot(0);
            if ((inputStack.isEmpty() || inputStack.isItemEqual(chamberStack)) && filterHandler.doesItemMatchFilter(chamberStack)) {
                int maxAllowedItems = Math.abs(core.getAirHandler(null).getAir()) / PneumaticValues.USAGE_CHAMBER_INTERFACE;
                if (maxAllowedItems > 0) {
                    if (!inputStack.isEmpty()) {
                        maxAllowedItems = Math.min(maxAllowedItems, chamberStack.getMaxStackSize() - inputStack.getCount());
                    }
                    int transferredItems = Math.min(chamberStack.getCount(), maxAllowedItems);
                    ItemStack toTransferStack = chamberStack.copy().split(transferredItems);
                    ItemStack excess = inventory.insertItem(0, toTransferStack, true);
                    if (excess.getCount() < toTransferStack.getCount()) {
                        // we can transfer at least some of the items
                        transferredItems = toTransferStack.getCount() - excess.getCount();
                        core.addAir((core.getAirHandler(null).getAir() > 0 ? -1 : 1) * transferredItems * PneumaticValues.USAGE_CHAMBER_INTERFACE);
                        toTransferStack.setCount(transferredItems);
                        inventory.insertItem(0, toTransferStack, false);
                        chamberStacks.extractItem(i, transferredItems, false);
                    }
                }
            }
        }
    }

    private void outputInChamber() {
        // place items from the interface block into the pressure chamber
        // all items in the interface will be moved at once, but the pressure chamber must have enough pressure to do so
        TileEntityPressureChamberValve valve = getCore();
        if (valve != null) {
            ItemStack inputStack = inventory.getStackInSlot(0);
            enoughAir = Math.abs(valve.getAirHandler(null).getAir()) > inputStack.getCount() * PneumaticValues.USAGE_CHAMBER_INTERFACE;
            if (enoughAir) {
                ItemStack leftover = ItemHandlerHelper.insertItem(valve.getStacksInChamber(), inputStack.copy(), false);
                int inserted = inputStack.getCount() - leftover.getCount();
                valve.addAir((valve.getAirHandler(null).getAir() > 0 ? -1 : 1) * inserted * PneumaticValues.USAGE_CHAMBER_INTERFACE);
                inventory.setStackInSlot(0, leftover);
            }
        }
    }

    // Figure out whether the Interface is exporting or importing.
    private InterfaceDirection getInterfaceMode(TileEntityPressureChamberValve core) {
        if (core != null) {
            boolean xMid = getPos().getX() != core.multiBlockX && getPos().getX() != core.multiBlockX + core.multiBlockSize - 1;
            boolean yMid = getPos().getY() != core.multiBlockY && getPos().getY() != core.multiBlockY + core.multiBlockSize - 1;
            boolean zMid = getPos().getZ() != core.multiBlockZ && getPos().getZ() != core.multiBlockZ + core.multiBlockSize - 1;
            Direction rotation = getRotation();
            if (xMid && yMid && rotation == Direction.NORTH || xMid && zMid && rotation == Direction.DOWN || yMid && zMid && rotation == Direction.WEST) {
                if (getPos().getX() == core.multiBlockX || getPos().getY() == core.multiBlockY || getPos().getZ() == core.multiBlockZ) {
                    return InterfaceDirection.EXPORT;
                } else {
                    return InterfaceDirection.IMPORT;
                }
            } else if (xMid && yMid && rotation == Direction.SOUTH || xMid && zMid && rotation == Direction.UP || yMid && zMid && rotation == Direction.EAST) {
                if (getPos().getX() == core.multiBlockX || getPos().getY() == core.multiBlockY || getPos().getZ() == core.multiBlockZ) {
                    return InterfaceDirection.IMPORT;
                } else {
                    return InterfaceDirection.EXPORT;
                }
            }
        }
        return InterfaceDirection.NONE;
    }

    public List<String> getProblemStat() {
        List<String> textList = new ArrayList<>();
        if (interfaceMode == InterfaceDirection.NONE) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The Interface can't work:"));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70\u2022 The Interface is not in a properly formed Pressure Chamber, and/or"));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70\u2022 The Interface is not adjacent to an air block of the Pressure Chamber, and/or"));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70\u2022 The Interface isn't oriented properly"));
        } else if (!enoughAir) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Not enough pressure in the Pressure Chamber to move the items."));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Apply more pressure to the Pressure Chamber. The required pressure is dependent on the amount of items being transported."));
        }
        return textList;
    }

    public boolean hasEnoughPressure() {
        return enoughAir;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        filterHandler.deserializeNBT(tag.getCompound("filter"));
        outputProgress = tag.getInt("outputProgress");
        inputProgress = tag.getInt("inputProgress");
        interfaceMode = InterfaceDirection.values()[tag.getInt("interfaceMode")];
        filterMode = FilterMode.values()[tag.getInt("filterMode")];
        itemNameFilter = tag.getString("itemNameFilter");
        redstoneMode = tag.getInt("redstoneMode");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.put("filter", filterHandler.serializeNBT());
        tag.putInt("outputProgress", outputProgress);
        tag.putInt("inputProgress", inputProgress);
        tag.putInt("interfaceMode", interfaceMode.ordinal());
        tag.putInt("filterMode", filterMode.ordinal());
        tag.putString("itemNameFilter", itemNameFilter);
        tag.putInt("redstoneMode", redstoneMode);
        return tag;
    }

    @Override
    public boolean isGuiUseableByPlayer(PlayerEntity player) {
        return getWorld().getTileEntity(getPos()) == this
                && player.getDistanceSq(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        } else if (tag.equals("filter_mode")) {
            int n = (filterMode.ordinal() + 1) % FilterMode.values().length;
            filterMode = FilterMode.values()[n];
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
    public LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return invCap;
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
                case NAME_BEGINS:
                    return asPlainString(itemStack.getDisplayName()).startsWith(itemNameFilter.toLowerCase());
                case NAME_CONTAINS:
                    return asPlainString(itemStack.getDisplayName()).contains(itemNameFilter.toLowerCase());
            }
            return false;
        }
    }

    private static String asPlainString(ITextComponent text) {
        return TextFormatting.getTextWithoutFormattingCodes(text.getFormattedText()).toLowerCase();
    }

    private class PressureChamberInterfaceHandler extends BaseItemStackHandler {
        PressureChamberInterfaceHandler() {
            super(TileEntityPressureChamberInterface.this, INVENTORY_SIZE);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return inputProgress == MAX_PROGRESS ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return outputProgress == MAX_PROGRESS ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if(!getWorld().isRemote && slot == 0) {
                sendDescriptionPacket();
            }
        }
    }
}
