package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerEtchingTank;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemEmptyPCB;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityEtchingTank extends TileEntityTickableBase
        implements INamedContainerProvider, ISerializableTanks, IHeatExchangingTE {
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

    public TileEntityEtchingTank() {
        super(ModTileEntities.ETCHING_TANK.get());

        heatExchanger.setThermalResistance(10);
        heatExchanger.setThermalCapacity(5);
    }

    @Override
    public void tick() {
        super.tick();

        acidTank.tick();

        if (!world.isRemote && !acidTank.getFluid().isEmpty()) {
            int tickInterval = getTickInterval();

            if (world.getGameTime() % tickInterval == 0) {
                boolean didWork = false;
                for (int i = 0; i < ETCHING_SLOTS; i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        int etchProgress = ItemEmptyPCB.getEtchProgress(stack);
                        if (etchProgress < 100) {
                            ItemEmptyPCB.setEtchProgress(stack, etchProgress + 1);
                            didWork = true;
                        } else if (!isOutputFull() && !isFailedOutputFull()) {
                            int uvProgress = TileEntityUVLightBox.getExposureProgress(stack);
                            boolean success = world.rand.nextInt(100) <= uvProgress;
                            tryMoveFinishedItem(i, success);
                        }
                    }
                }

                if (didWork && tickInterval < 30) {
                    // heated - chance to use up some acid
                    if (world.rand.nextInt(100) < 30 - tickInterval) {
                        acidTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
                    }
                    heatExchanger.addHeat(-(30 - tickInterval));
                }
            }
        }
    }

    public int getTickInterval() {
        int delta = MathHelper.clamp(heatExchanger.getTemperatureAsInt() - 323, 0, 480);
        return 30 - (delta + 1) / 20;
    }

    private void tryMoveFinishedItem(int slot, boolean success) {
        ItemStack stack = itemHandler.extractItem(slot, 1, true);
        if (!stack.isEmpty()) {
            ItemStack excess;
            if (success) {
                excess = outputHandler.insertItem(0, new ItemStack(ModItems.UNASSEMBLED_PCB.get()), false);
            } else {
                excess = failedHandler.insertItem(0, new ItemStack(ModItems.FAILED_PCB.get()), false);
            }
            if (excess.isEmpty()) {
                itemHandler.extractItem(slot, 1, false);
            }
        }
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

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return itemCap;
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

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        return heatCap;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, itemCap);
            } else if (side.getAxis() == Direction.Axis.Y) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, endCap);
            } else {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, sideCap);
            }
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, fluidCap);
        } /*else if (cap == PNCCapabilities.HEAT_EXCHANGER_CAPABILITY) {
            return PNCCapabilities.HEAT_EXCHANGER_CAPABILITY.orEmpty(cap, heatCap);
        }*/
        return super.getCapability(cap, side);
    }

    @Override
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        super.getContentsToDrop(drops);

        PneumaticCraftUtils.collectNonEmptyItems(outputHandler, drops);
        PneumaticCraftUtils.collectNonEmptyItems(failedHandler, drops);
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
        return new ContainerEtchingTank(windowId, playerInv, getPos());
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", acidTank);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.put("Inventory", itemHandler.serializeNBT());
        tag.put("Output", outputHandler.serializeNBT());
        tag.put("Failed", failedHandler.serializeNBT());

        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

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
            super(TileEntityEtchingTank.this, ETCHING_SLOTS);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() == ModItems.EMPTY_PCB.get() && TileEntityUVLightBox.getExposureProgress(stack) > 0;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }

    private class OutputItemHandler extends BaseItemStackHandler {
        OutputItemHandler() {
            super(TileEntityEtchingTank.this, 1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() == ModItems.UNASSEMBLED_PCB.get();
        }
    }

    private class FailedItemHandler extends BaseItemStackHandler {
        FailedItemHandler() {
            super(TileEntityEtchingTank.this, 1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() == ModItems.FAILED_PCB.get();
        }
    }

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
            super(TileEntityEtchingTank.this, 4000);
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isIn(PneumaticCraftTags.Fluids.ETCHING_ACID);
        }
    }
}
