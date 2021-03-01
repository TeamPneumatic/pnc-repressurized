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
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public abstract class TileEntityFluidTank extends TileEntityTickableBase
        implements ISerializableTanks, INamedContainerProvider, IComparatorSupport {
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

    TileEntityFluidTank(TileEntityType<?> type, BlockFluidTank.Size tankSize) {
        super(type, 4);

        this.tank = new StackableTank(tankSize.getCapacity());
        this.fluidCap = LazyOptional.of(() -> tank);
    }

    @Override
    public void tick() {
        super.tick();

        tank.tick();

        if (!world.isRemote) {
            processFluidItem(INPUT_SLOT, OUTPUT_SLOT);

            FluidStack stack = getTank().getFluid();
            if (!stack.isEmpty()) {
                Direction dir = stack.getFluid().getAttributes().getDensity() < 0 ? Direction.UP : Direction.DOWN;
                if (getBlockState().get(BlockPneumaticCraft.connectionProperty(dir))) {
                    BlockState other = world.getBlockState(pos.offset(dir));
                    if (other.getBlock() instanceof BlockFluidTank && other.get(BlockPneumaticCraft.connectionProperty(dir.getOpposite()))) {
                        TileEntity teOther = getCachedNeighbor(dir);
                        if (teOther instanceof TileEntityFluidTank) {
                            FluidUtil.tryFluidTransfer(((TileEntityFluidTank) teOther).getTank(), tank, tank.getCapacity() / 32, true);
                        }
                    }
                }
            }

            Direction ejectDir = getUpgradeCache().getEjectDirection();
            if (ejectDir != null && (ejectDir.getAxis() != Direction.Axis.Y || !getBlockState().get(BlockPneumaticCraft.connectionProperty(ejectDir)))) {
                IOHelper.getFluidHandlerForTE(getCachedNeighbor(ejectDir), ejectDir.getOpposite()).ifPresent(h -> {
                    int amount = BASE_EJECT_RATE << getUpgrades(EnumUpgrade.SPEED);
                    FluidUtil.tryFluidTransfer(h, tank, amount, true);
                });
            }
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
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of(ItemBlockFluidTank.TANK_NAME, tank);
    }

    public SmartSyncTank getTank() {
        return tank;
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
        return new ContainerFluidTank(windowId, inv, getPos());
    }

    public boolean isNeighbourCompatible(FluidStack stack, Direction dir) {
        BlockState state = getBlockState();
        TileEntityFluidTank curTank = this;
        while (state.getBlock() instanceof BlockFluidTank) {
            if (!state.get(BlockPneumaticCraft.connectionProperty(dir))) {
                // no connection? no problem
                return true;
            }
            TileEntity teOther = curTank.getCachedNeighbor(dir);
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
        public Small() {
            super(ModTileEntities.TANK_SMALL.get(), BlockFluidTank.Size.SMALL);
        }
    }

    public static class Medium extends TileEntityFluidTank {
        public Medium() {
            super(ModTileEntities.TANK_MEDIUM.get(), BlockFluidTank.Size.MEDIUM);
        }
    }

    public static class Large extends TileEntityFluidTank {
        public Large() {
            super(ModTileEntities.TANK_LARGE.get(), BlockFluidTank.Size.LARGE);
        }
    }

    public static class Huge extends TileEntityFluidTank {
        public Huge() {
            super(ModTileEntities.TANK_HUGE.get(), BlockFluidTank.Size.HUGE);
        }
    }
}
