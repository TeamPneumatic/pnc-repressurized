package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.BlockLiquidHopper;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidHopper;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BucketItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityLiquidHopper extends TileEntityAbstractHopper implements ISerializableTanks {
    private int comparatorValue = -1;

    @DescSynced
    @GuiSynced
    private final HopperTank tank = new HopperTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    private final LazyOptional<IFluidHandler> tankCap = LazyOptional.of(() -> tank);
    private final WrappedFluidTank inputWrapper = new WrappedFluidTank(tank, true);
    private final LazyOptional<IFluidHandler> inputCap = LazyOptional.of(() -> inputWrapper);
    private final WrappedFluidTank outputWrapper = new WrappedFluidTank(tank, false);
    private final LazyOptional<IFluidHandler> outputCap = LazyOptional.of(() -> outputWrapper);

    public TileEntityLiquidHopper() {
        super(ModTileEntities.LIQUID_HOPPER.get());
    }

    @Override
    protected int getComparatorValueInternal() {
        if (comparatorValue < 0) {
            if (tank.getFluidAmount() == 0) return 0;
            FluidStack fluidStack = tank.getFluid();
            comparatorValue = (int) (1 + ((float) fluidStack.getAmount() / tank.getCapacity() * 14f));
        }
        return comparatorValue;
    }

    @Override
    public void tick() {
        super.tick();

        tank.tick();

        if (!world.isRemote && getUpgrades(EnumUpgrade.CREATIVE) > 0) {
            FluidStack fluidStack = tank.getFluid();
            if (!fluidStack.isEmpty() && fluidStack.getAmount() < PneumaticValues.NORMAL_TANK_CAPACITY) {
                tank.fill(new FluidStack(fluidStack.getFluid(), PneumaticValues.NORMAL_TANK_CAPACITY), FluidAction.EXECUTE);
            }
        }
    }

    @Override
    protected boolean doExport(int maxItems) {
        if (tank.getFluid().isEmpty()) return false;

        Direction dir = getRotation();

        // try to fill any neighbouring fluid-accepting tile entity
        TileEntity neighbor = getCachedNeighbor(dir);
        if (neighbor != null) {
            return IOHelper.getFluidHandlerForTE(neighbor, dir.getOpposite()).map(fluidHandler -> {
                int amount = Math.min(maxItems * 100, tank.getFluid().getAmount() - leaveMaterialCount * 1000);
                FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, tank, amount, true);
                return !transferred.isEmpty();
            }).orElse(false);
        }

        // try to fill any fluid-handling items in front of the output
        if (getWorld().isAirBlock(getPos().offset(dir))) {
            for (ItemEntity entity : getNeighborItems(this, dir)) {
                FluidActionResult res = FluidUtil.tryFillContainer(entity.getItem(), tank, maxItems * 100, null, true);
                if (res.success) {
                    entity.setItem(res.result);
                    break;
                }
            }
        }

        // try to pour fluid into the world
        if (PNCConfig.Common.Machines.liquidHopperDispenser && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            return FluidUtils.tryPourOutFluid(getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir),
                    world, getPos().offset(dir), false, false, FluidAction.EXECUTE);
        }

        return false;
    }

    @Override
    protected boolean doImport(int maxItems) {
        TileEntity inputInv = getCachedNeighbor(inputDir);

        if (inputInv != null) {
            LazyOptional<IFluidHandler> cap = inputInv.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inputDir.getOpposite());
            if (cap.isPresent()) {
                return cap.map(fluidHandler -> {
                    FluidStack fluid = fluidHandler.drain(maxItems * 100, FluidAction.SIMULATE);
                    if (fluid != null) {
                        int filledFluid = tank.fill(fluid, FluidAction.EXECUTE);
                        if (filledFluid > 0) {
                            fluidHandler.drain(filledFluid, FluidAction.EXECUTE);
                            return true;
                        }
                    }
                    return false;
                }).orElse(false);
            }
        }

        if (getWorld().isAirBlock(getPos().offset(inputDir))) {
            for (ItemEntity entity : getNeighborItems(this, inputDir)) {
                // special case for buckets, which can only transfer 1000mB at a time
                int max = entity.getItem().getItem() instanceof BucketItem ? 1000 : maxItems * 100;
                FluidActionResult res = FluidUtil.tryEmptyContainer(entity.getItem(), tank, max, null, true);
                if (res.success) {
                    entity.setItem(res.result);
                    return true;
                }
            }
        }

        if (PNCConfig.Common.Machines.liquidHopperDispenser && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            BlockPos neighborPos = getPos().offset(inputDir);
            LazyOptional<IFluidHandler> cap = getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inputDir);
            return !FluidUtils.tryPickupFluid(cap, world, neighborPos, false, FluidAction.EXECUTE).isEmpty();
        }

        return false;
    }

    public HopperTank getTank() {
        return tank;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        comparatorValue = -1;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (facing == inputDir) {
                return inputCap.cast();
            } else if (facing == getRotation()) {
                return outputCap.cast();
            } else {
                return tankCap.cast();
            }
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of(BlockLiquidHopper.ItemBlockLiquidHopper.TANK_NAME, tank);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerLiquidHopper(i, playerInventory, getPos());
    }

    public class HopperTank extends SmartSyncTank {
        HopperTank(int capacity) {
            super(TileEntityLiquidHopper.this, capacity);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            comparatorValue = -1;
        }

        @Override
        public int fill(FluidStack resource, FluidAction doFill) {
            int filled = super.fill(resource, doFill);
            if (isCreative && getFluidAmount() > 0 && getFluid().getFluid() == resource.getFluid()) {
                return resource.getAmount();   // acts like an infinite fluid sink
            } else {
                return filled;
            }
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            return super.drain(resource, isCreative ? FluidAction.SIMULATE : doDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            return super.drain(maxDrain, isCreative ? FluidAction.SIMULATE : doDrain);
        }
    }

    class WrappedFluidTank implements IFluidTank, IFluidHandler {
        private final FluidTank wrappedTank;
        private final boolean inbound;

        WrappedFluidTank(FluidTank wrappedTank, boolean inbound) {
            // inbound == true: fill *only*, inbound == false: drain *only*
            this.wrappedTank = wrappedTank;
            this.inbound = inbound;
        }

        @Override
        public FluidStack getFluid() {
            return wrappedTank.getFluid();
        }

        @Override
        public int getFluidAmount() {
            return wrappedTank.getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return wrappedTank.getCapacity();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return wrappedTank.isFluidValid(stack);
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return wrappedTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return wrappedTank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return wrappedTank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction doFill) {
            return inbound ? wrappedTank.fill(resource, doFill) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            return inbound ? FluidStack.EMPTY : tank.drain(resource, doDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            return inbound ? FluidStack.EMPTY : wrappedTank.drain(maxDrain, doDrain);
        }
    }
}
