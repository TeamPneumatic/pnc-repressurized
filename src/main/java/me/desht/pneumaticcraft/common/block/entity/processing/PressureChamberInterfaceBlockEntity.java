/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block.entity.processing;

import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.block.entity.IRedstoneControl;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController;
import me.desht.pneumaticcraft.common.inventory.PressureChamberInterfaceMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.AcceptabilityCache;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

public class PressureChamberInterfaceBlockEntity extends PressureChamberWallBlockEntity
        implements IRedstoneControl<PressureChamberInterfaceBlockEntity>, MenuProvider {
    public static final int MAX_PROGRESS = 40;
    public static final int INVENTORY_SIZE = 1;
    private static final int MIN_SOUND_INTERVAL = 400;  // ticks - the sound effect is ~2.5s long

    // cache items we know are accepted to reduce recipe searching
    private static final AcceptabilityCache<Item> acceptedItemCache = new AcceptabilityCache<>();

    @DescSynced
    private final PressureChamberInterfaceHandler inventory = new PressureChamberInterfaceHandler();
    @DescSynced
    private float doorSpeed = 1f;
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
    public final RedstoneController<PressureChamberInterfaceBlockEntity> rsController = new RedstoneController<>(this);
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

    public PressureChamberInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.PRESSURE_CHAMBER_INTERFACE.get(), pos, state, 4);
    }

    public static void clearCachedItems() {
        acceptedItemCache.clear();
    }

    @Override
    public boolean hasItemCapability() {
        return true;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new PressureChamberInterfaceMenu(i, playerInventory, getBlockPos());
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        boolean wasOpeningI = isOpeningInput;
        boolean wasOpeningO = isOpeningOutput;
        oldInputProgress = inputProgress;
        oldOutputProgress = outputProgress;
        PressureChamberValveBlockEntity core = getPrimaryValve();

        if (!nonNullLevel().isClientSide) {
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

        if (nonNullLevel().isClientSide && soundTimer++ >= MIN_SOUND_INTERVAL && (wasOpeningI != isOpeningInput || wasOpeningO != isOpeningOutput)) {
            nonNullLevel().playLocalSound(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, ModSounds.INTERFACE_DOOR.get(), SoundSource.BLOCKS, 0.5F, 1.0F, true);
            soundTimer = 0;
        }
    }

    public ItemStack getStackInInterface() {
        return inventory.getStackInSlot(0);
    }

    private void exportToInventory() {
        Direction facing = getRotation();
        BlockEntity te = getCachedNeighbor(facing);
        ItemStack stack = inventory.getStackInSlot(0);
        if (te != null) {
            int count = stack.getCount();
            ItemStack leftoverStack = IOHelper.insert(te, stack.copy(), facing.getOpposite(), false);
            stack.shrink(count - leftoverStack.getCount());
        } else if (getUpgrades(ModUpgrades.DISPENSER.get()) > 0) {
            BlockPos pos = getBlockPos().relative(getRotation());
            PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            inventory.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    private void importFromChamber(PressureChamberValveBlockEntity core) {
        IItemHandler chamberHandler = exportAny ? core.allItems : core.craftedItems;
        for (int i = 0; i < chamberHandler.getSlots(); i++) {
            ItemStack chamberStack = chamberHandler.getStackInSlot(i);
            if (chamberStack.isEmpty()) {
                continue;
            }
            ItemStack stackInInterface = inventory.getStackInSlot(0);
            if ((stackInInterface.isEmpty() || ItemStack.isSameItem(stackInInterface, chamberStack))) {
                IAirHandlerMachine coreAirHandler = Objects.requireNonNull(core.getAirHandler(null));
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
        PressureChamberValveBlockEntity valve = getPrimaryValve();
        if (valve != null) {
            ItemStack inputStack = inventory.getStackInSlot(0);
            IAirHandlerMachine valveAirHandler = Objects.requireNonNull(valve.getAirHandler(null));
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
    private InterfaceDirection getInterfaceMode(PressureChamberValveBlockEntity core) {
        if (core != null) {
            boolean xMid = getBlockPos().getX() != core.multiBlockX && getBlockPos().getX() != core.multiBlockX + core.multiBlockSize - 1;
            boolean yMid = getBlockPos().getY() != core.multiBlockY && getBlockPos().getY() != core.multiBlockY + core.multiBlockSize - 1;
            boolean zMid = getBlockPos().getZ() != core.multiBlockZ && getBlockPos().getZ() != core.multiBlockZ + core.multiBlockSize - 1;
            Direction rotation = getRotation();
            if (xMid && yMid && rotation == Direction.NORTH || xMid && zMid && rotation == Direction.DOWN || yMid && zMid && rotation == Direction.WEST) {
                if (getBlockPos().getX() == core.multiBlockX || getBlockPos().getY() == core.multiBlockY || getBlockPos().getZ() == core.multiBlockZ) {
                    return InterfaceDirection.EXPORT;
                } else {
                    return InterfaceDirection.IMPORT;
                }
            } else if (xMid && yMid && rotation == Direction.SOUTH || xMid && zMid && rotation == Direction.UP || yMid && zMid && rotation == Direction.EAST) {
                if (getBlockPos().getX() == core.multiBlockX || getBlockPos().getY() == core.multiBlockY || getBlockPos().getZ() == core.multiBlockZ) {
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
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        inventory.deserializeNBT(provider, tag.getCompound("Items"));
        outputProgress = tag.getFloat("outputProgress");
        inputProgress = tag.getFloat("inputProgress");
        interfaceMode = InterfaceDirection.values()[tag.getInt("interfaceMode")];
        exportAny = tag.getBoolean("exportAny");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("Items", inventory.serializeNBT(provider));
        tag.putFloat("outputProgress", outputProgress);
        tag.putFloat("inputProgress", inputProgress);
        tag.putInt("interfaceMode", interfaceMode.ordinal());
        tag.putBoolean("exportAny", exportAny);
    }

    @Override
    public boolean isGuiUseableByPlayer(Player player) {
        return nonNullLevel().getBlockEntity(getBlockPos()) == this
                && player.distanceToSqr(getBlockPos().getX() + 0.5D, getBlockPos().getY() + 0.5D, getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return inventory;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag))
            return;
        if (tag.equals("export_mode")) {
            exportAny = !exportAny;
            setChanged();
        }
    }

    @Override
    public RedstoneController<PressureChamberInterfaceBlockEntity> getRedstoneController() {
        return rsController;
    }

    private class PressureChamberInterfaceHandler extends BaseItemStackHandler {
        PressureChamberInterfaceHandler() {
            super(PressureChamberInterfaceBlockEntity.this, INVENTORY_SIZE);
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
            if (PressureChamberInterfaceBlockEntity.this.interfaceMode == InterfaceDirection.IMPORT) {
                return acceptedItemCache.isAcceptable(stack.getItem(), () ->
                        ModRecipeTypes.PRESSURE_CHAMBER.get().stream(level)
                                .map(RecipeHolder::value)
                                .anyMatch(recipe -> recipe.isValidInputItem(stack))
                );
            } else return PressureChamberInterfaceBlockEntity.this.interfaceMode == InterfaceDirection.EXPORT;
        }
    }
}
