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

package me.desht.pneumaticcraft.common.block.entity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.EtchingTankMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.EmptyPCBItem;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class EtchingTankBlockEntity extends AbstractTickingBlockEntity
        implements MenuProvider, ISerializableTanks, IHeatExchangingTE {
    public static final int ETCHING_SLOTS = 25;

    private final EtchingTankHandler itemHandler = new EtchingTankHandler();
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> itemHandler);

    private final OutputItemHandler outputHandler = new OutputItemHandler();
    private final FailedItemHandler failedHandler = new FailedItemHandler();

    private final WrappedInvHandler sideHandler = new WrappedInvHandler(outputHandler);
    private final LazyOptional<IItemHandler> sideCap = LazyOptional.of(() -> sideHandler);
    private final WrappedInvHandler endHandler = new WrappedInvHandler(failedHandler);
    private final LazyOptional<IItemHandler> endCap = LazyOptional.of(() -> endHandler);

    @DescSynced
    @GuiSynced
    private final EtchingFluidTank acidTank = new EtchingFluidTank();
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> acidTank);

    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final LazyOptional<IHeatExchangerLogic> heatCap = LazyOptional.of(() -> heatExchanger);

    public EtchingTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ETCHING_TANK.get(), pos, state);

        heatExchanger.setThermalResistance(10);
        heatExchanger.setThermalCapacity(5);
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        acidTank.tick();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (!acidTank.getFluid().isEmpty()) {
            int tickInterval = getTickInterval();

            final Level level = nonNullLevel();
            if (level.getGameTime() % tickInterval == 0) {
                boolean didWork = false;
                for (int i = 0; i < ETCHING_SLOTS; i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        int etchProgress = EmptyPCBItem.getEtchProgress(stack);
                        if (etchProgress < 100) {
                            EmptyPCBItem.setEtchProgress(stack, etchProgress + 1);
                            didWork = true;
                        } else if (!isOutputFull() && !isFailedOutputFull()) {
                            int uvProgress = UVLightBoxBlockEntity.getExposureProgress(stack);
                            boolean success = level.random.nextInt(100) <= uvProgress;
                            tryMoveFinishedItem(i, success);
                        }
                    }
                }

                if (didWork && tickInterval < 30) {
                    // heated - chance to use up some acid
                    if (level.random.nextInt(100) < 30 - tickInterval) {
                        acidTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
                    }
                    heatExchanger.addHeat(-(30 - tickInterval));
                }
            }
        }
    }

    public int getTickInterval() {
        int delta = Mth.clamp(heatExchanger.getTemperatureAsInt() - 323, 0, 480);
        return 30 - (delta + 1) / 20;
    }

    private void tryMoveFinishedItem(int slot, boolean success) {
        ItemStack inputStack = itemHandler.extractItem(slot, 1, true);
        if (!inputStack.isEmpty()) {
            ItemStack result = getResultItem(inputStack, success);
            ItemStack excess;
            if (success) {
                excess = outputHandler.insertItem(0, result, false);
            } else {
                excess = failedHandler.insertItem(0, result, false);
            }
            if (excess.isEmpty()) {
                itemHandler.extractItem(slot, 1, false);
            }
        }
    }

    private ItemStack getResultItem(ItemStack inputStack, boolean success) {
        if (inputStack.getItem() instanceof EmptyPCBItem emptyPCB) {
            return success ? emptyPCB.getSuccessItem() : emptyPCB.getFailedItem();
        }
        // shouldn't happen, but just in case
        return success ? ModItems.EMPTY_PCB.get().getSuccessItem() : ModItems.EMPTY_PCB.get().getFailedItem();
    }

    public boolean isOutputFull() {
        ItemStack stack = outputHandler.getStackInSlot(0);
        return stack.getCount() >= stack.getMaxStackSize();
    }

    public boolean isFailedOutputFull() {
        ItemStack stack = failedHandler.getStackInSlot(0);
        return stack.getCount() >= stack.getMaxStackSize();
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return itemHandler;
    }

    public OutputItemHandler getOutputHandler() {
        return outputHandler;
    }

    public FailedItemHandler getFailedHandler() {
        return failedHandler;
    }

    public IFluidTank getAcidTank() {
        return acidTank;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap(Direction side) {
        if (side == null) {
            return itemCap;
        } else if (side.getAxis() == Direction.Axis.Y) {
            return endCap;
        } else {
            return sideCap;
        }
    }

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        return heatCap;
    }

    @NotNull
    @Override
    public LazyOptional<IFluidHandler> getFluidCap(Direction side) {
        return fluidCap;
    }

    @Override
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        super.getContentsToDrop(drops);

        PneumaticCraftUtils.collectNonEmptyItems(outputHandler, drops);
        PneumaticCraftUtils.collectNonEmptyItems(failedHandler, drops);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return new EtchingTankMenu(windowId, playerInv, getBlockPos());
    }

    @Nonnull
    @Override
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", acidTank);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("Inventory", itemHandler.serializeNBT());
        tag.put("Output", outputHandler.serializeNBT());
        tag.put("Failed", failedHandler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        outputHandler.deserializeNBT(tag.getCompound("Output"));
        failedHandler.deserializeNBT(tag.getCompound("Failed"));
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }

    private class EtchingTankHandler extends BaseItemStackHandler {
        EtchingTankHandler() {
            super(EtchingTankBlockEntity.this, ETCHING_SLOTS);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof EmptyPCBItem && UVLightBoxBlockEntity.getExposureProgress(stack) > 0;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }

    private class OutputItemHandler extends BaseItemStackHandler {
        OutputItemHandler() {
            super(EtchingTankBlockEntity.this, 1);
        }
    }

    private class FailedItemHandler extends BaseItemStackHandler {
        FailedItemHandler() {
            super(EtchingTankBlockEntity.this, 1);
        }
    }

    /**
     * Wrapped item handler exposed via capability. Slot 0 is the output slot (success for side access, failed for
     * top/bottom access), and slots 1 -> ETCHING_SLOTS + 1 are the input slots.
     */
    private class WrappedInvHandler implements IItemHandler {
        private final IItemHandler output;

        WrappedInvHandler(IItemHandler output) {
            this.output = output;
        }

        @Override
        public int getSlots() {
            return ETCHING_SLOTS + 1;  // either the success or failed slot
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? output.getStackInSlot(0) : itemHandler.getStackInSlot(slot - 1);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return slot == 0 ? stack : itemHandler.insertItem(slot - 1, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 0 ? output.extractItem(0, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? output.getSlotLimit(0) : itemHandler.getSlotLimit(slot - 1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 ? output.isItemValid(0, stack) : itemHandler.isItemValid(slot - 1, stack);
        }
    }

    private class EtchingFluidTank extends SmartSyncTank {
        EtchingFluidTank() {
            super(EtchingTankBlockEntity.this, 4000);
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().is(PneumaticCraftTags.Fluids.ETCHING_ACID);
        }
    }
}
