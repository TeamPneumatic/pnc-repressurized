package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerPressureChamberInterface;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TileEntityPressureChamberInterface extends TileEntityPressureChamberWall
        implements ITickableTileEntity, IRedstoneControl<TileEntityPressureChamberInterface>, INamedContainerProvider {
    public static final int MAX_PROGRESS = 40;
    public static final int INVENTORY_SIZE = 1;
    private static final int MIN_SOUND_INTERVAL = 400;  // ticks - the sound effect is ~2.5s long

    // cache items we know are accepted to reduce recipe searching
    private static final Set<Item> acceptedItemCache = new HashSet<>();

    @DescSynced
    private final PressureChamberInterfaceHandler inventory = new PressureChamberInterfaceHandler();
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);
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
    public final RedstoneController<TileEntityPressureChamberInterface> rsController = new RedstoneController<>(this);
    private int inputTimeOut;
    private int oldItemCount;
    @GuiSynced
    public boolean exportAny;

    public enum InterfaceDirection implements ITranslatableEnum {
        NONE, IMPORT, EXPORT;

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.pressureChamberInterface.mode." + toString().toLowerCase(Locale.ROOT);
        }
    }

    public TileEntityPressureChamberInterface() {
        super(ModTileEntities.PRESSURE_CHAMBER_INTERFACE.get(), 4);
    }

    public static void clearCachedItems() {
        acceptedItemCache.clear();
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
                        if (interfaceMode == InterfaceDirection.EXPORT && inputProgress == MAX_PROGRESS && rsController.shouldRun()) {
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
        IItemHandlerModifiable chamberHandler = exportAny ? core.allItems : core.craftedItems;
        for (int i = 0; i < chamberHandler.getSlots(); i++) {
            ItemStack chamberStack = chamberHandler.getStackInSlot(i);
            if (chamberStack.isEmpty()) {
                continue;
            }
            ItemStack stackInInterface = inventory.getStackInSlot(0);
            if ((stackInInterface.isEmpty() || stackInInterface.isItemEqual(chamberStack))) {
                IAirHandlerMachine coreAirHandler = core.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY)
                        .orElseThrow(RuntimeException::new);
                int maxAllowedItems = Math.abs(coreAirHandler.getAir()) / PneumaticValues.USAGE_CHAMBER_INTERFACE;
                if (maxAllowedItems > 0) {
                    maxAllowedItems = Math.min(maxAllowedItems, chamberStack.getMaxStackSize() - stackInInterface.getCount());
                    int transferredItems = Math.min(chamberStack.getCount(), maxAllowedItems);
                    if (transferredItems > 0) {
                        ItemStack toTransferStack = chamberStack.copy().split(transferredItems);
                        ItemStack excess = inventory.insertItem(0, toTransferStack, true);
                        if (excess.getCount() < toTransferStack.getCount()) {
                            // we can transfer at least some of the items
                            transferredItems = toTransferStack.getCount() - excess.getCount();
                            core.addAir((coreAirHandler.getAir() > 0 ? -1 : 1) * transferredItems * PneumaticValues.USAGE_CHAMBER_INTERFACE);
                            toTransferStack.setCount(transferredItems);
                            inventory.insertItem(0, toTransferStack, false);
                            chamberHandler.extractItem(i, transferredItems, false);
                        }
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
            IAirHandlerMachine valveAirHandler = valve.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY)
                    .orElseThrow(RuntimeException::new);
            enoughAir = Math.abs(valveAirHandler.getAir()) > inputStack.getCount() * PneumaticValues.USAGE_CHAMBER_INTERFACE;
            if (enoughAir) {
                ItemStack excess = valve.insertItemToChamber(inputStack);
                int inserted = inputStack.getCount() - excess.getCount();
                valve.addAir((valveAirHandler.getAir() > 0 ? -1 : 1) * inserted * PneumaticValues.USAGE_CHAMBER_INTERFACE);
                inventory.setStackInSlot(0, excess);
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

    public boolean hasEnoughPressure() {
        return enoughAir;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        outputProgress = tag.getFloat("outputProgress");
        inputProgress = tag.getFloat("inputProgress");
        interfaceMode = InterfaceDirection.values()[tag.getInt("interfaceMode")];
        exportAny = tag.getBoolean("exportAny");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putFloat("outputProgress", outputProgress);
        tag.putFloat("inputProgress", inputProgress);
        tag.putInt("interfaceMode", interfaceMode.ordinal());
        tag.putBoolean("exportAny", exportAny);
        return tag;
    }

    @Override
    public boolean isGuiUseableByPlayer(PlayerEntity player) {
        return getWorld().getTileEntity(getPos()) == this
                && player.getDistanceSq(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        if (rsController.parseRedstoneMode(tag))
            return;
        if (tag.equals("export_mode")) {
            exportAny = !exportAny;
            markDirty();
        }
    }

    @Override
    public RedstoneController<TileEntityPressureChamberInterface> getRedstoneController() {
        return rsController;
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    private class PressureChamberInterfaceHandler extends BaseItemStackHandler {
        PressureChamberInterfaceHandler() {
            super(TileEntityPressureChamberInterface.this, INVENTORY_SIZE);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return inputProgress == MAX_PROGRESS && isValidItem(stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return outputProgress == MAX_PROGRESS ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        private boolean isValidItem(ItemStack stack) {
            if (TileEntityPressureChamberInterface.this.interfaceMode == InterfaceDirection.IMPORT) {
                if (acceptedItemCache.contains(stack.getItem())) return true;
                boolean accepted = PneumaticCraftRecipeType.PRESSURE_CHAMBER.stream(world)
                        .anyMatch(recipe -> recipe.isValidInputItem(stack));
                if (accepted) acceptedItemCache.add(stack.getItem());
                return accepted;
            } else return TileEntityPressureChamberInterface.this.interfaceMode == InterfaceDirection.EXPORT;
        }
    }
}
