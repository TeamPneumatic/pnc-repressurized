package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityPressureChamberInterface extends TileEntityPressureChamberWall implements ITickableTileEntity, IRedstoneControlled, INamedContainerProvider {
    public static final int MAX_PROGRESS = 40;
    public static final int INVENTORY_SIZE = 1;
    private static final int MIN_SOUND_INTERVAL = 400;  // ticks - the sound effect is ~2.5s long

    @DescSynced
    private final PressureChamberInterfaceHandler inventory = new PressureChamberInterfaceHandler();
    private final LazyOptional<IItemHandlerModifiable> invCap = LazyOptional.of(() -> inventory);
    @DescSynced
    private float doorSpeed = 0f;
    @DescSynced
    @LazySynced
    public float inputProgress;
    public float oldInputProgress;
    @DescSynced
    @LazySynced
    public float outputProgress;
    public float oldOutputProgress;
    @GuiSynced
    public InterfaceDirection interfaceMode = InterfaceDirection.NONE;
    @GuiSynced
    private boolean enoughAir = true;
    private boolean isOpeningInput; // used to determine sounds.
    private boolean isOpeningOutput; // used to determine sounds.
    private int soundTimer;
    @DescSynced
    private boolean shouldOpenInput, shouldOpenOutput;
    @GuiSynced
    public int redstoneMode;
    private int inputTimeOut;
    private int oldItemCount;
    @GuiSynced
    public boolean exportAny;

    public enum InterfaceDirection {
        NONE, IMPORT, EXPORT;

        public String getTranslationKey() {
            return "gui.pressureChamberInterface.mode." + toString().toLowerCase();
        }
    }

    public TileEntityPressureChamberInterface() {
        super(ModTileEntities.PRESSURE_CHAMBER_INTERFACE.get(), 4);
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

        boolean wasOpeningI = isOpeningInput;
        boolean wasOpeningO = isOpeningOutput;
        oldInputProgress = inputProgress;
        oldOutputProgress = outputProgress;
        TileEntityPressureChamberValve core = getCore();

        if (!getWorld().isRemote) {
            doorSpeed = getSpeedMultiplierFromUpgrades();

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


        if (shouldOpenInput) {
            inputProgress = Math.min(inputProgress + doorSpeed, MAX_PROGRESS);
            isOpeningInput = true;
        } else {
            inputProgress = Math.max(inputProgress - doorSpeed, 0);
            isOpeningInput = false;
        }

        if (shouldOpenOutput) {
            outputProgress = Math.min(outputProgress + doorSpeed, MAX_PROGRESS);
            isOpeningOutput = true;
        } else {
            outputProgress = Math.max(outputProgress - doorSpeed, 0);
            isOpeningOutput = false;
        }

        if (getWorld().isRemote && soundTimer++ >= MIN_SOUND_INTERVAL && (wasOpeningI != isOpeningInput || wasOpeningO != isOpeningOutput)) {
            getWorld().playSound(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, ModSounds.INTERFACE_DOOR.get(), SoundCategory.BLOCKS, 0.5F, 1.0F, true);
            soundTimer = 0;
        }
    }

    public ItemStack getStackInInterface() {
        return inventory.getStackInSlot(0);
    }

    private void exportToInventory() {
        Direction facing = getRotation();
        TileEntity te = getCachedNeighbor(facing);
        ItemStack stack = inventory.getStackInSlot(0);
        if (te != null) {
            int count = stack.getCount();
            ItemStack leftoverStack = IOHelper.insert(te, stack.copy(), facing.getOpposite(), false);
            stack.shrink(count - leftoverStack.getCount());
        } else if (getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            BlockPos pos = getPos().offset(getRotation());
            PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            inventory.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    private void importFromChamber(TileEntityPressureChamberValve core) {
        ItemStackHandler chamberStacks = core.getStacksInChamber();
        for (int i = 0; i < chamberStacks.getSlots(); i++) {
            ItemStack chamberStack = chamberStacks.getStackInSlot(i);
            if (chamberStack.isEmpty()) {
                continue;
            }
            ItemStack stackInInterface = inventory.getStackInSlot(0);
            if ((stackInInterface.isEmpty() || stackInInterface.isItemEqual(chamberStack)) && canPullItem(chamberStack)) {
                final int idx = i;
                core.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(coreAirHandler -> {
                    int maxAllowedItems = Math.abs(coreAirHandler.getAir()) / PneumaticValues.USAGE_CHAMBER_INTERFACE;
                    if (maxAllowedItems > 0) {
                        maxAllowedItems = Math.min(maxAllowedItems, chamberStack.getMaxStackSize() - stackInInterface.getCount());
                        int transferredItems = Math.min(chamberStack.getCount(), maxAllowedItems);
                        ItemStack toTransferStack = chamberStack.copy().split(transferredItems);
                        ItemStack excess = inventory.insertItem(0, toTransferStack, true);
                        if (excess.getCount() < toTransferStack.getCount()) {
                            // we can transfer at least some of the items
                            transferredItems = toTransferStack.getCount() - excess.getCount();
                            core.addAir((coreAirHandler.getAir() > 0 ? -1 : 1) * transferredItems * PneumaticValues.USAGE_CHAMBER_INTERFACE);
                            toTransferStack.setCount(transferredItems);
                            inventory.insertItem(0, toTransferStack, false);
                            chamberStacks.extractItem(idx, transferredItems, false);
                        }
                    }
                });
            }
        }
    }

    private boolean canPullItem(ItemStack stack) {
        if (exportAny) return true;

        for (IPressureChamberRecipe recipe: PneumaticCraftRecipes.pressureChamberRecipes.values()) {
            if (recipe.isOutputItem(stack)) {
                return true;
            }
        }

        return false;
    }

    private void outputInChamber() {
        // place items from the interface block into the pressure chamber
        // all items in the interface will be moved at once, but the pressure chamber must have enough pressure to do so
        TileEntityPressureChamberValve valve = getCore();
        if (valve != null) {
            ItemStack inputStack = inventory.getStackInSlot(0);
            valve.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(valveAirHandler -> {
                enoughAir = Math.abs(valveAirHandler.getAir()) > inputStack.getCount() * PneumaticValues.USAGE_CHAMBER_INTERFACE;
                if (enoughAir) {
                    ItemStack leftover = ItemHandlerHelper.insertItem(valve.getStacksInChamber(), inputStack.copy(), false);
                    int inserted = inputStack.getCount() - leftover.getCount();
                    valve.addAir((valveAirHandler.getAir() > 0 ? -1 : 1) * inserted * PneumaticValues.USAGE_CHAMBER_INTERFACE);
                    inventory.setStackInSlot(0, leftover);
                }
            });
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
            textList.addAll(PneumaticCraftUtils.splitString("\u00a77The Interface can't work:"));
            textList.addAll(PneumaticCraftUtils.splitString("\u00a70\u2022 The Interface is not in a properly formed Pressure Chamber, and/or"));
            textList.addAll(PneumaticCraftUtils.splitString("\u00a70\u2022 The Interface is not adjacent to an air block of the Pressure Chamber, and/or"));
            textList.addAll(PneumaticCraftUtils.splitString("\u00a70\u2022 The Interface isn't oriented properly"));
        } else if (!enoughAir) {
            textList.addAll(PneumaticCraftUtils.splitString("\u00a70Not enough pressure in the Pressure Chamber to move the items."));
            textList.addAll(PneumaticCraftUtils.splitString("\u00a70Apply more pressure to the Pressure Chamber. The required pressure is dependent on the amount of items being transported."));
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
        outputProgress = tag.getFloat("outputProgress");
        inputProgress = tag.getFloat("inputProgress");
        interfaceMode = InterfaceDirection.values()[tag.getInt("interfaceMode")];
        exportAny = tag.getBoolean("exportAny");
        redstoneMode = tag.getInt("redstoneMode");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putFloat("outputProgress", outputProgress);
        tag.putFloat("inputProgress", inputProgress);
        tag.putInt("interfaceMode", interfaceMode.ordinal());
        tag.putBoolean("exportAny", exportAny);
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
        } else if (tag.equals("export_mode")) {
            exportAny = !exportAny;
        }
        markDirty();
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    protected LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return invCap;
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
            if (!getWorld().isRemote && slot == 0) {
                sendDescriptionPacket();
            }
        }
    }
}
