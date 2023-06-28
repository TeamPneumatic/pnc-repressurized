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
import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.common.block.FluidTankBlock;
import me.desht.pneumaticcraft.common.block.FluidTankBlock.ItemBlockFluidTank;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.inventory.FluidTankMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public abstract class AbstractFluidTankBlockEntity extends AbstractTickingBlockEntity
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

    AbstractFluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, FluidTankBlock.Size tankSize) {
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
            Direction dir = stack.getFluid().getFluidType().isLighterThanAir() ? Direction.UP : Direction.DOWN;
            if (getBlockState().getValue(AbstractPneumaticCraftBlock.connectionProperty(dir))) {
                BlockState other = nonNullLevel().getBlockState(worldPosition.relative(dir));
                if (other.getBlock() instanceof FluidTankBlock && other.getValue(AbstractPneumaticCraftBlock.connectionProperty(dir.getOpposite()))) {
                    BlockEntity teOther = getCachedNeighbor(dir);
                    if (teOther instanceof AbstractFluidTankBlockEntity) {
                        FluidUtil.tryFluidTransfer(((AbstractFluidTankBlockEntity) teOther).getTank(), tank, tank.getCapacity() / 32, true);
                    }
                }
            }
        }

        Direction ejectDir = getUpgradeCache().getEjectDirection();
        if (ejectDir != null && (ejectDir.getAxis() != Direction.Axis.Y || !getBlockState().getValue(AbstractPneumaticCraftBlock.connectionProperty(ejectDir)))) {
            IOHelper.getFluidHandlerForTE(getCachedNeighbor(ejectDir), ejectDir.getOpposite()).ifPresent(h -> {
                int amount = BASE_EJECT_RATE << getUpgrades(ModUpgrades.SPEED.get());
                FluidUtil.tryFluidTransfer(h, tank, amount, true);
            });
        }
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap(Direction side) {
        return inventoryCap;
    }

    @NotNull
    @Override
    public LazyOptional<IFluidHandler> getFluidCap(Direction side) {
        return fluidCap;
    }

    @Nonnull
    @Override
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of(ItemBlockFluidTank.TANK_NAME, tank);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("Items", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
    }

    public SmartSyncTank getTank() {
        return tank;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new FluidTankMenu(windowId, inv, getBlockPos());
    }

    public boolean isNeighbourCompatible(FluidStack stack, Direction dir) {
        BlockState state = getBlockState();
        AbstractFluidTankBlockEntity curTank = this;
        while (state.getBlock() instanceof FluidTankBlock) {
            if (!state.getValue(AbstractPneumaticCraftBlock.connectionProperty(dir))) {
                // no connection? no problem
                return true;
            }
            BlockEntity teOther = curTank.getCachedNeighbor(dir);
            if (teOther instanceof AbstractFluidTankBlockEntity) {
                curTank = (AbstractFluidTankBlockEntity) teOther;
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
            super(AbstractFluidTankBlockEntity.this, capacity);
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

    public static class Small extends AbstractFluidTankBlockEntity {
        public Small(BlockPos pos, BlockState state) {
            super(ModBlockEntities.TANK_SMALL.get(), pos, state, FluidTankBlock.Size.SMALL);
        }
    }

    public static class Medium extends AbstractFluidTankBlockEntity {
        public Medium(BlockPos pos, BlockState state) {
            super(ModBlockEntities.TANK_MEDIUM.get(), pos, state, FluidTankBlock.Size.MEDIUM);
        }
    }

    public static class Large extends AbstractFluidTankBlockEntity {
        public Large(BlockPos pos, BlockState state) {
            super(ModBlockEntities.TANK_LARGE.get(), pos, state, FluidTankBlock.Size.LARGE);
        }
    }

    public static class Huge extends AbstractFluidTankBlockEntity {
        public Huge(BlockPos pos, BlockState state) {
            super(ModBlockEntities.TANK_HUGE.get(), pos, state, FluidTankBlock.Size.HUGE);
        }
    }
}
