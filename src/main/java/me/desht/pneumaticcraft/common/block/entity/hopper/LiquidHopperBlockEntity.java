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

package me.desht.pneumaticcraft.common.block.entity.hopper;

import me.desht.pneumaticcraft.common.block.entity.ISerializableTanks;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController;
import me.desht.pneumaticcraft.common.block.entity.SmartSyncTank;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.inventory.LiquidHopperMenu;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class LiquidHopperBlockEntity extends AbstractHopperBlockEntity<LiquidHopperBlockEntity> implements ISerializableTanks {
    private int comparatorValue = -1;

    @DescSynced
    @GuiSynced
    private final HopperTank tank = new HopperTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    private final WrappedFluidTank inputWrapper = new WrappedFluidTank(tank, true);
    private final WrappedFluidTank outputWrapper = new WrappedFluidTank(tank, false);
    @GuiSynced
    private final RedstoneController<LiquidHopperBlockEntity> rsController = new RedstoneController<>(this);

    private final Lazy<BlockCapabilityCache<IFluidHandler,Direction>> inputCache = Lazy.of(() -> createFluidHandlerCache(inputDir));
    private final Lazy<BlockCapabilityCache<IFluidHandler,Direction>> outputCache = Lazy.of(() -> createFluidHandlerCache(getRotation()));

    public LiquidHopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.LIQUID_HOPPER.get(), pos, state);
    }

    private BlockCapabilityCache<IFluidHandler,Direction> getInputCache() {
        return inputCache.get();
    }

    private BlockCapabilityCache<IFluidHandler,Direction> getOutputCache() {
        return outputCache.get();
    }

    @Override
    public boolean hasFluidCapability() {
        return true;
    }

    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction dir) {
        if (dir == inputDir) {
            return inputWrapper;
        } else if (dir == getRotation()) {
            return outputWrapper;
        } else {
            return tank;
        }
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
    public void tickCommonPre() {
        super.tickCommonPre();

        tank.tick();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (getUpgrades(ModUpgrades.CREATIVE.get()) > 0) {
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

        // try to fill any neighbouring fluid-accepting block entity
        IFluidHandler dstHandler = getOutputCache().getCapability();
        if (dstHandler != null) {
            int amount = Math.min(maxItems * 100, tank.getFluid().getAmount() - leaveMaterialCount * 1000);
            FluidStack transferred = FluidUtil.tryFluidTransfer(dstHandler, tank, amount, true);
            return !transferred.isEmpty();
        } else if (getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) > 0) {
            for (Entity e : cachedOutputEntities) {
                if (!e.isAlive()) continue;
                FluidStack transferred = IOHelper.getFluidHandlerForEntity(e, getRotation().getOpposite()).map(h -> {
                    int amount = Math.min(maxItems * 100, tank.getFluid().getAmount() - leaveMaterialCount * 1000);
                    return FluidUtil.tryFluidTransfer(h, tank, amount, true);
                }).orElse(FluidStack.EMPTY);
                if (!transferred.isEmpty()) return true;
            }
        }

        // try to fill any fluid-handling items in front of the output
        for (Entity e : cachedOutputEntities) {
            if (e.isAlive() && e instanceof ItemEntity entity) {
                int maxFill = entity.getItem().getItem() instanceof BucketItem ? 1000 : maxItems * 100;
                FluidActionResult res = FluidUtil.tryFillContainer(entity.getItem(), tank, maxFill, null, true);
                if (res.success) {
                    entity.setItem(res.result);
                }
                if (tank.isEmpty()) break;
            }
        }

        // try to pour fluid into the world
        if (ConfigHelper.common().machines.liquidHopperDispenser.get() && getUpgrades(ModUpgrades.DISPENSER.get()) > 0
                && tank.getFluidAmount() >= leaveMaterialCount + FluidType.BUCKET_VOLUME) {
            return FluidUtils.tryPourOutFluid(outputWrapper, nonNullLevel(), getBlockPos().relative(dir), false, false, FluidAction.EXECUTE);
        }

        return false;
    }

    @Override
    protected boolean doImport(int maxItems) {
        IFluidHandler srcHandler = getInputCache().getCapability();
        if (srcHandler != null) {
            FluidStack fluid = srcHandler.drain(maxItems * 100, FluidAction.SIMULATE);
            if (!fluid.isEmpty()) {
                int filledFluid = tank.fill(fluid, FluidAction.EXECUTE);
                if (filledFluid > 0) {
                    srcHandler.drain(filledFluid, FluidAction.EXECUTE);
                    return true;
                }
            }
            return false;
        } else if (getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) > 0) {
            for (Entity e : cachedInputEntities) {
                if (!e.isAlive()) continue;
                FluidStack transferred = IOHelper.getFluidHandlerForEntity(e, inputDir.getOpposite())
                        .map(h -> FluidUtil.tryFluidTransfer(tank, h, maxItems * 100, true))
                        .orElse(FluidStack.EMPTY);
                if (!transferred.isEmpty()) return true;
            }
        }

        for (Entity e : cachedInputEntities) {
            if (e.isAlive() && e instanceof ItemEntity entity) {
                // special case: buckets can only drain 1000 mB at a time
                int max = entity.getItem().getItem() instanceof BucketItem ? FluidType.BUCKET_VOLUME : maxItems * 100;
                FluidActionResult res = FluidUtil.tryEmptyContainer(entity.getItem(), tank, max, null, true);
                if (res.success) {
                    entity.setItem(res.result);
                }
                if (tank.getFluidAmount() >= tank.getCapacity()) break;
            }
        }

        if (ConfigHelper.common().machines.liquidHopperDispenser.get() && getUpgrades(ModUpgrades.DISPENSER.get()) > 0) {
            BlockPos neighborPos = getBlockPos().relative(inputDir);
            return !FluidUtils.tryPickupFluid(inputWrapper, nonNullLevel(), neighborPos, false, FluidAction.EXECUTE).isEmpty();
        }

        return false;
    }

    @Override
    protected void setupInputOutputRegions() {
        inputAABB = new AABB(worldPosition.relative(inputDir));
        outputAABB = new AABB(getBlockPos().relative(getRotation()));

        inputCache.invalidate();
        outputCache.invalidate();

        cachedInputEntities.clear();
        cachedOutputEntities.clear();
    }

    @Override
    boolean shouldScanForEntities(Direction dir) {
        BlockEntity te = getCachedNeighbor(dir);
        return (te == null || IOHelper.getFluidHandlerForBlock(te, dir.getOpposite()).isEmpty())
                && !isInputBlocked();
    }

    public HopperTank getTank() {
        return tank;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        comparatorValue = -1;
    }

    @Override
    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return null;
    }

    @Nonnull
    @Override
    public Map<DataComponentType<SimpleFluidContent>, PNCFluidTank> getSerializableTanks() {
        return Map.of(ModDataComponents.MAIN_TANK.get(), tank);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new LiquidHopperMenu(i, playerInventory, getBlockPos());
    }

    @Override
    public RedstoneController<LiquidHopperBlockEntity> getRedstoneController() {
        return rsController;
    }

    public class HopperTank extends SmartSyncTank {
        HopperTank(int capacity) {
            super(LiquidHopperBlockEntity.this, capacity);
        }

        @Override
        protected void onContentsChanged(FluidStack prevStack) {
            comparatorValue = -1;
            super.onContentsChanged(prevStack);
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
