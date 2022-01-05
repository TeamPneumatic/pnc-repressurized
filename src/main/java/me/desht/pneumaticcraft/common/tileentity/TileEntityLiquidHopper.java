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
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BucketItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityLiquidHopper extends TileEntityAbstractHopper<TileEntityLiquidHopper> implements ISerializableTanks {
    private int comparatorValue = -1;

    @DescSynced
    @GuiSynced
    private final HopperTank tank = new HopperTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    private final LazyOptional<IFluidHandler> tankCap = LazyOptional.of(() -> tank);
    private final WrappedFluidTank inputWrapper = new WrappedFluidTank(tank, true);
    private final LazyOptional<IFluidHandler> inputCap = LazyOptional.of(() -> inputWrapper);
    private final WrappedFluidTank outputWrapper = new WrappedFluidTank(tank, false);
    private final LazyOptional<IFluidHandler> outputCap = LazyOptional.of(() -> outputWrapper);
    @GuiSynced
    private final RedstoneController<TileEntityLiquidHopper> rsController = new RedstoneController<>(this);

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

        if (!level.isClientSide && getUpgrades(EnumUpgrade.CREATIVE) > 0) {
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
        } else if (getUpgrades(EnumUpgrade.ENTITY_TRACKER) > 0) {
            for (Entity e : cachedOutputEntities) {
                if (!e.isAlive()) continue;
                FluidStack transferred = e.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getRotation().getOpposite()).map(h -> {
                    int amount = Math.min(maxItems * 100, tank.getFluid().getAmount() - leaveMaterialCount * 1000);
                    return FluidUtil.tryFluidTransfer(h, tank, amount, true);
                }).orElse(FluidStack.EMPTY);
                if (!transferred.isEmpty()) return true;
            }
        }

        // try to fill any fluid-handling items in front of the output
        for (Entity e : cachedOutputEntities) {
            if (e.isAlive() && e instanceof ItemEntity) {
                ItemEntity entity = (ItemEntity) e;
                int maxFill = entity.getItem().getItem() instanceof BucketItem ? 1000 : maxItems * 100;
                FluidActionResult res = FluidUtil.tryFillContainer(entity.getItem(), tank, maxFill, null, true);
                if (res.success) {
                    entity.setItem(res.result);
                }
                if (tank.isEmpty()) break;
            }
        }

        // try to pour fluid into the world
        if (PNCConfig.Common.Machines.liquidHopperDispenser && getUpgrades(EnumUpgrade.DISPENSER) > 0
                && tank.getFluidAmount() >= leaveMaterialCount + FluidAttributes.BUCKET_VOLUME) {
            return FluidUtils.tryPourOutFluid(outputCap, level, getBlockPos().relative(dir), false, false, FluidAction.EXECUTE);
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
                    if (!fluid.isEmpty()) {
                        int filledFluid = tank.fill(fluid, FluidAction.EXECUTE);
                        if (filledFluid > 0) {
                            fluidHandler.drain(filledFluid, FluidAction.EXECUTE);
                            return true;
                        }
                    }
                    return false;
                }).orElse(false);
            }
        } else if (getUpgrades(EnumUpgrade.ENTITY_TRACKER) > 0) {
            for (Entity e : cachedInputEntities) {
                if (!e.isAlive()) continue;
                FluidStack transferred = e.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inputDir.getOpposite())
                        .map(h -> FluidUtil.tryFluidTransfer(tank, h, maxItems * 100, true))
                        .orElse(FluidStack.EMPTY);
                if (!transferred.isEmpty()) return true;
            }
        }

        for (Entity e : cachedInputEntities) {
            if (e.isAlive() && e instanceof ItemEntity) {
                ItemEntity entity = (ItemEntity) e;
                // special case: buckets can only drain 1000 mB at a time
                int max = entity.getItem().getItem() instanceof BucketItem ? FluidAttributes.BUCKET_VOLUME : maxItems * 100;
                FluidActionResult res = FluidUtil.tryEmptyContainer(entity.getItem(), tank, max, null, true);
                if (res.success) {
                    entity.setItem(res.result);
                }
                if (tank.getFluidAmount() >= tank.getCapacity()) break;
            }
        }

        if (PNCConfig.Common.Machines.liquidHopperDispenser && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            BlockPos neighborPos = getBlockPos().relative(inputDir);
            return !FluidUtils.tryPickupFluid(inputCap, level, neighborPos, false, FluidAction.EXECUTE).isEmpty();
        }

        return false;
    }

    @Override
    protected void setupInputOutputRegions() {
        inputAABB = new AxisAlignedBB(worldPosition.relative(inputDir));
        outputAABB = new AxisAlignedBB(getBlockPos().relative(getRotation()));

        cachedInputEntities.clear();
        cachedOutputEntities.clear();
    }

    @Override
    boolean shouldScanForEntities(Direction dir) {
        TileEntity te = getCachedNeighbor(dir);
        return (te == null || !te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite()).isPresent())
                && !Block.canSupportCenter(level, worldPosition.relative(dir), dir.getOpposite());
    }

    public HopperTank getTank() {
        return tank;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

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
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of(BlockLiquidHopper.ItemBlockLiquidHopper.TANK_NAME, tank);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerLiquidHopper(i, playerInventory, getBlockPos());
    }

    @Override
    public RedstoneController<TileEntityLiquidHopper> getRedstoneController() {
        return rsController;
    }

    public class HopperTank extends SmartSyncTank {
        HopperTank(int capacity) {
            super(TileEntityLiquidHopper.this, capacity);
        }

        @Override
        protected void onContentsChanged(Fluid prevFluid, int prevAmount) {
            super.onContentsChanged(prevFluid, prevAmount);
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
        private final IFluidTank wrappedTank;
        private final boolean inbound;

        WrappedFluidTank(IFluidTank wrappedTank, boolean inbound) {
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
