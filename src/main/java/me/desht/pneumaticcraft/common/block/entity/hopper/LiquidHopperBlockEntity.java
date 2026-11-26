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
import me.desht.pneumaticcraft.common.inventory.handler.IOCombinedHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class LiquidHopperBlockEntity extends AbstractHopperBlockEntity<LiquidHopperBlockEntity> implements ISerializableTanks {
    private int comparatorValue = -1;

    @DescSynced
    @GuiSynced
    private final HopperTank tank = new HopperTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    private final ResourceHandler<FluidResource> inputWrapper = new IOCombinedHandler<>(t -> true, t -> false, tank);  // fill only
    private final ResourceHandler<FluidResource> outputWrapper = new IOCombinedHandler<>(t -> false, t -> true, tank);  // drain only
    @GuiSynced
    private final RedstoneController<LiquidHopperBlockEntity> rsController = new RedstoneController<>(this);

    private final Lazy<BlockCapabilityCache<ResourceHandler<FluidResource>,Direction>> inputCache = Lazy.of(() -> createFluidHandlerCache(inputDir));
    private final Lazy<BlockCapabilityCache<ResourceHandler<FluidResource>,Direction>> outputCache = Lazy.of(() -> createFluidHandlerCache(getRotation()));

    public LiquidHopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.LIQUID_HOPPER.get(), pos, state);
    }

    private BlockCapabilityCache<ResourceHandler<FluidResource>,Direction> getInputCache() {
        return inputCache.get();
    }

    private BlockCapabilityCache<ResourceHandler<FluidResource>,Direction> getOutputCache() {
        return outputCache.get();
    }

    @Override
    public boolean hasFluidCapability() {
        return true;
    }

    @Override
    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction dir) {
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
                FluidUtils.fill(tank, new FluidStack(fluidStack.getFluid(), PneumaticValues.NORMAL_TANK_CAPACITY), false);
            }
        }
    }

    @Override
    protected boolean doExport(int maxItems) {
        if (tank.getFluid().isEmpty()) return false;

        Direction dir = getRotation();

        // try to fill any neighbouring fluid-accepting block entity
        ResourceHandler<FluidResource> dstHandler = IOHelper.getCapability(getOutputCache());
        if (dstHandler != null) {
            int amount = Math.min(maxItems * 100, tank.getFluid().getAmount() - leaveMaterialCount * 1000);
            FluidStack transferred = FluidUtils.transfer(tank, dstHandler, amount, false);
            return !transferred.isEmpty();
        } else if (getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) > 0) {
            for (Entity e : cachedOutputEntities) {
                if (!e.isAlive()) continue;
                FluidStack transferred = IOHelper.getFluidHandlerForEntity(e, getRotation().getOpposite()).map(h -> {
                    int amount = Math.min(maxItems * 100, tank.getFluid().getAmount() - leaveMaterialCount * 1000);
                    return FluidUtils.transfer(tank, h, amount, false);
                }).orElse(FluidStack.EMPTY);
                if (!transferred.isEmpty()) return true;
            }
        }

        // try to fill any fluid-handling items in front of the output
        for (Entity e : cachedOutputEntities) {
            if (e.isAlive() && e instanceof ItemEntity entity) {
                FluidUtils.transferWithItemEntity(entity, tank, maxItems * 100, true);
                if (tank.isEmpty()) break;
            }
        }

        // try to pour fluid into the world
        if (ConfigHelper.common().machines.liquidHopperDispenser.get() && getUpgrades(ModUpgrades.DISPENSER.get()) > 0
                && tank.getFluidAmount() >= leaveMaterialCount + FluidType.BUCKET_VOLUME) {
            return FluidUtils.tryPourOutFluid(outputWrapper, nonNullLevel(), getBlockPos().relative(dir), false, false, false);
        }

        return false;
    }

    @Override
    protected boolean doImport(int maxItems) {
        ResourceHandler<FluidResource> srcHandler = IOHelper.getCapability(getInputCache());
        if (srcHandler != null) {
            FluidStack fluid = FluidUtils.drain(srcHandler, maxItems * 100, true);
            if (!fluid.isEmpty()) {
                int filledFluid = FluidUtils.fill(tank, fluid, false);
                if (filledFluid > 0) {
                    FluidUtils.drain(srcHandler, filledFluid, false);
                    return true;
                }
            }
            return false;
        } else if (getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) > 0) {
            for (Entity e : cachedInputEntities) {
                if (!e.isAlive()) continue;
                FluidStack transferred = IOHelper.getFluidHandlerForEntity(e, inputDir.getOpposite())
                        .map(h -> FluidUtils.transfer(h, tank, maxItems * 100, false))
                        .orElse(FluidStack.EMPTY);
                if (!transferred.isEmpty()) return true;
            }
        }

        for (Entity e : cachedInputEntities) {
            if (e.isAlive()) {
                if (e instanceof ItemEntity entity) {
                    FluidUtils.transferWithItemEntity(entity, tank, maxItems * 100, false);
                    if (tank.getFluidAmount() >= tank.getCapacity()) break;
                } else if (e instanceof ExperienceOrb orb && getUpgrades(ModUpgrades.DISPENSER.get()) > 0) {
                    if (orb.getValue() <= 0 || PneumaticCraftUtils.fillTankWithOrb(tank, orb, false)) {
                        orb.discard();
                    }
                }
            }
        }

        if (ConfigHelper.common().machines.liquidHopperDispenser.get() && getUpgrades(ModUpgrades.DISPENSER.get()) > 0) {
            BlockPos neighborPos = getBlockPos().relative(inputDir);
            return !FluidUtils.tryPickupFluid(inputWrapper, nonNullLevel(), neighborPos, false, false).isEmpty();
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
    public ResourceHandler<ItemResource> getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
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
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            int filled = super.insert(index, resource, amount, transaction);
            if (isCreative && getFluidAmount() > 0 && getFluid().getFluid() == resource.getFluid()) {
                return amount;   // acts like an infinite fluid sink
            } else {
                return filled;
            }
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (isCreative) {
                // acts like an infinite fluid source
                return resource.matches(getFluid()) ? Math.min(amount, getFluidAmount()) : 0;
            }
            return super.extract(index, resource, amount, transaction);
        }
    }
}
