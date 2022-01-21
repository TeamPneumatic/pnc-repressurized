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
import me.desht.pneumaticcraft.common.block.BlockFluidTank;
import me.desht.pneumaticcraft.common.block.BlockFluidTank.ItemBlockFluidTank;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerFluidTank;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public abstract class TileEntityFluidTank extends TileEntityTickableBase
        implements ISerializableTanks, MenuProvider, IComparatorSupport {
    private static final int INVENTORY_SIZE = 2;
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int BASE_EJECT_RATE = 25;

    @LazySynced
    @DescSynced
    @GuiSynced
    private final StackableTank tank;
    private final LazyOptional<IFluidHandler> fluidCap;

    private final ItemStackHandler inventory = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || FluidUtil.getFluidHandler(itemStack).isPresent();
        }
    };
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);

    TileEntityFluidTank(BlockEntityType<?> type, BlockPos pos, BlockState state, BlockFluidTank.Size tankSize) {
        super(type, pos, state, 4);

        this.tank = new StackableTank(tankSize.getCapacity());
        this.fluidCap = LazyOptional.of(() -> tank);
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        tank.tick();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        processFluidItem(INPUT_SLOT, OUTPUT_SLOT);

        FluidStack stack = getTank().getFluid();
        if (!stack.isEmpty()) {
            Direction dir = stack.getFluid().getAttributes().getDensity() < 0 ? Direction.UP : Direction.DOWN;
            if (getBlockState().getValue(BlockPneumaticCraft.connectionProperty(dir))) {
                BlockState other = nonNullLevel().getBlockState(worldPosition.relative(dir));
                if (other.getBlock() instanceof BlockFluidTank && other.getValue(BlockPneumaticCraft.connectionProperty(dir.getOpposite()))) {
                    BlockEntity teOther = getCachedNeighbor(dir);
                    if (teOther instanceof TileEntityFluidTank) {
                        FluidUtil.tryFluidTransfer(((TileEntityFluidTank) teOther).getTank(), tank, tank.getCapacity() / 32, true);
                    }
                }
            }
        }

        Direction ejectDir = getUpgradeCache().getEjectDirection();
        if (ejectDir != null && (ejectDir.getAxis() != Direction.Axis.Y || !getBlockState().getValue(BlockPneumaticCraft.connectionProperty(ejectDir)))) {
            IOHelper.getFluidHandlerForTE(getCachedNeighbor(ejectDir), ejectDir.getOpposite()).ifPresent(h -> {
                int amount = BASE_EJECT_RATE << getUpgrades(EnumUpgrade.SPEED);
                FluidUtil.tryFluidTransfer(h, tank, amount, true);
            });
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, fluidCap);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventoryCap;
    }

    @Nonnull
    @Override
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of(ItemBlockFluidTank.TANK_NAME, tank);
    }

    public SmartSyncTank getTank() {
        return tank;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new ContainerFluidTank(windowId, inv, getBlockPos());
    }

    public boolean isNeighbourCompatible(FluidStack stack, Direction dir) {
        BlockState state = getBlockState();
        TileEntityFluidTank curTank = this;
        while (state.getBlock() instanceof BlockFluidTank) {
            if (!state.getValue(BlockPneumaticCraft.connectionProperty(dir))) {
                // no connection? no problem
                return true;
            }
            BlockEntity teOther = curTank.getCachedNeighbor(dir);
            if (teOther instanceof TileEntityFluidTank) {
                curTank = (TileEntityFluidTank) teOther;
                state = curTank.getBlockState();
                if (!isFluidCompatible(stack, curTank.getTank())) {
                    return false;
                }
            } else {
                // shouldn't get here, but if we do we can assume the tank is not actually connected this way
                return true;
            }
        }
        return true;
    }

    public boolean isFluidCompatible(FluidStack stack, IFluidTank tank) {
        return stack.isEmpty() || tank.getFluid().isEmpty() || stack.getFluid() == tank.getFluid().getFluid();
    }

    public class StackableTank extends SmartSyncTank {
        StackableTank(int capacity) {
            super(TileEntityFluidTank.this, capacity);
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return isNeighbourCompatible(stack, Direction.UP) && isNeighbourCompatible(stack, Direction.DOWN);
        }
    }

    @Override
    public int getComparatorValue() {
        return tank.isEmpty() ? 0 : 1 + (tank.getFluidAmount() * 14) / tank.getCapacity();
    }

    public static class Small extends TileEntityFluidTank {
        public Small(BlockPos pos, BlockState state) {
            super(ModTileEntities.TANK_SMALL.get(), pos, state, BlockFluidTank.Size.SMALL);
        }
    }

    public static class Medium extends TileEntityFluidTank {
        public Medium(BlockPos pos, BlockState state) {
            super(ModTileEntities.TANK_MEDIUM.get(), pos, state, BlockFluidTank.Size.MEDIUM);
        }
    }

    public static class Large extends TileEntityFluidTank {
        public Large(BlockPos pos, BlockState state) {
            super(ModTileEntities.TANK_LARGE.get(), pos, state, BlockFluidTank.Size.LARGE);
        }
    }

    public static class Huge extends TileEntityFluidTank {
        public Huge(BlockPos pos, BlockState state) {
            super(ModTileEntities.TANK_HUGE.get(), pos, state, BlockFluidTank.Size.HUGE);
        }
    }
}
