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
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.ai.ChunkPositionSorter;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerGasLift;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered;
import me.desht.pneumaticcraft.common.util.*;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileEntityGasLift extends TileEntityPneumaticBase implements
        IMinWorkingPressure, IRedstoneControl<TileEntityGasLift>, ISerializableTanks,
        IAutoFluidEjecting, MenuProvider
{
    private static final int INVENTORY_SIZE = 1;

    public enum Status implements ITranslatableEnum {
        IDLE("idling"), PUMPING("pumping"), DIGGING("diggingDown"), RETRACTING("retracting"), STUCK("stuck");

        private final String desc;

        Status(String desc) {
            this.desc = desc;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tab.status.gasLift.action." + desc;
        }
    }

    public enum PumpMode {
        PUMP_EMPTY, PUMP_LEAVE_FLUID, RETRACT
    }

    @DescSynced
    @GuiSynced
    private final GasLiftFluidTank tank = new GasLiftFluidTank();
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);

    private final ItemStackHandler inventory = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() == ModBlocks.DRILL_PIPE.get().asItem();
        }
    };
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);
    @GuiSynced
    public int currentDepth;
    @GuiSynced
    public final RedstoneController<TileEntityGasLift> rsController = new RedstoneController<>(this);
    @GuiSynced
    public PumpMode pumpMode = PumpMode.PUMP_EMPTY;
    @GuiSynced
    public Status status = Status.IDLE;
    private int workTimer;
    private int ticker;
    private List<BlockPos> pumpingLake;
    private static final int MAX_PUMP_RANGE_SQUARED = 15 * 15;

    public TileEntityGasLift(BlockPos pos, BlockState state) {
        super(ModTileEntities.GAS_LIFT.get(), pos, state, PressureTier.TIER_ONE, 3000, 4);
    }

    @Override
    public boolean canConnectPneumatic(Direction d) {
        return d != Direction.DOWN;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        tank.tick();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        ticker++;
        if (currentDepth > 0) {
            int curCheckingPipe = ticker % currentDepth;
            if (curCheckingPipe > 0 && !isPipe(nonNullLevel(), getBlockPos().relative(Direction.DOWN, curCheckingPipe))) {
                currentDepth = curCheckingPipe - 1;
            }
        }
        if (ticker == 400) {
            pumpingLake = null;
            ticker = 0;
        }

        if (rsController.shouldRun() && getPressure() >= getMinWorkingPressure()) {
            workTimer += this.getSpeedMultiplierFromUpgrades();
            while (workTimer > 20) {
                workTimer -= 20;
                status = Status.IDLE;
                if (pumpMode == PumpMode.RETRACT) {
                    retractPipes();
                } else {
                    if (!suckLiquid() && !tryDigDown()) {
                        break;
                    }
                }
            }
        } else {
            status = Status.IDLE;
        }
    }

    private void retractPipes() {
        if (currentDepth > 0) {
            status = Status.RETRACTING;
            final Level level = nonNullLevel();
            if (isPipe(level, getBlockPos().offset(0, -currentDepth, 0))) {
                BlockPos pos1 = getBlockPos().relative(Direction.DOWN, currentDepth);
                ItemStack toInsert = new ItemStack(level.getBlockState(pos1).getBlock());
                if (inventory.insertItem(0, toInsert, true).isEmpty()) {
                    inventory.insertItem(0, toInsert, false);
                    level.destroyBlock(pos1, false);
                    addAir(-100);
                    currentDepth--;
                } else {
                    status = Status.IDLE;
                }
            } else {
                currentDepth--;
            }
        }
    }

    private boolean tryDigDown() {
        if (isUnbreakable(getBlockPos().relative(Direction.DOWN, currentDepth + 1))) {
            status = Status.STUCK;
        } else if (getBlockPos().getY() - currentDepth >= 0) {
            status = Status.DIGGING;
            currentDepth++;
            BlockPos pos1 = getBlockPos().relative(Direction.DOWN, currentDepth);
            final Level level = nonNullLevel();
            if (!isPipe(level, pos1)) {
                ItemStack extracted = inventory.extractItem(0, 1, true);
                if (extracted.getItem() == ModBlocks.DRILL_PIPE.get().asItem()) {
                    BlockState currentState = level.getBlockState(pos1);
                    BlockState newState = ((BlockItem) extracted.getItem()).getBlock().defaultBlockState();

                    int airRequired = Math.round(66.66f * currentState.getDestroySpeed(level, pos1));
                    if (airHandler.getAir() > airRequired) {
                        inventory.extractItem(0, 1, false);
                        level.destroyBlock(pos1, false);
                        level.setBlockAndUpdate(pos1, newState);
                        // kludge: don't permit placing more than one tube per tick
                        // causes TE cache problems - root cause to be determined
                        workTimer = 19;
                        addAir(-airRequired);
                    } else {
                        status = Status.IDLE;
                        currentDepth--;
                    }
                } else {
                    status = Status.IDLE;
                    currentDepth--;
                }
            }
        } else {
            status = Status.IDLE;
        }
        return status == Status.DIGGING;
    }

    private boolean isPipe(Level world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == ModBlocks.DRILL_PIPE.get();
    }

    private boolean isUnbreakable(BlockPos pos) {
        return nonNullLevel().getBlockState(pos).getDestroySpeed(nonNullLevel(), pos) < 0;
    }

    private boolean suckLiquid() {
        BlockPos pos = getBlockPos().relative(Direction.DOWN, currentDepth + 1);

        final Level level = nonNullLevel();
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.getType() == Fluids.EMPTY) {
            pumpingLake = null;
            return false;
        }
        FluidStack fluidStack = new FluidStack(fluidState.getType(), FluidAttributes.BUCKET_VOLUME);

        if (tank.fill(fluidStack, FluidAction.SIMULATE) == fluidStack.getAmount()) {
            if (pumpingLake == null) {
                findLake(fluidStack.getFluid());
            }
            boolean foundSource = false;
            BlockPos curPos = null;
            while (pumpingLake.size() > 0) {
                curPos = pumpingLake.get(0);
                if (FluidUtils.isSourceFluidBlock(level, curPos, fluidStack.getFluid())) {
                    foundSource = true;
                    break;
                }
                pumpingLake.remove(0);
            }
            if (pumpingLake.isEmpty()) {
                pumpingLake = null;
            } else if (foundSource) {
                FluidStack taken = FluidUtils.tryPickupFluid(fluidCap, level, curPos, false, FluidAction.EXECUTE);
                if (taken.getAmount() == FluidAttributes.BUCKET_VOLUME) {
                    addAir(-100);
                    status = Status.PUMPING;
                }
            }
        }
        return true;
    }

    private void findLake(Fluid fluid) {
        pumpingLake = new ArrayList<>();
        Stack<BlockPos> pendingPositions = new Stack<>();
        BlockPos thisPos = getBlockPos().relative(Direction.DOWN, currentDepth + 1);
        pendingPositions.add(thisPos);
        pumpingLake.add(thisPos);
        while (!pendingPositions.empty()) {
            BlockPos checkingPos = pendingPositions.pop();
            for (Direction d : DirectionUtil.VALUES) {
                if (d == Direction.DOWN) continue;
                BlockPos newPos = checkingPos.relative(d);
                if (PneumaticCraftUtils.distBetweenSq(newPos, thisPos) <= MAX_PUMP_RANGE_SQUARED
                        && FluidUtils.isSourceFluidBlock(nonNullLevel(), newPos, fluid)
                        && !pumpingLake.contains(newPos)) {
                    pendingPositions.add(newPos);
                    pumpingLake.add(newPos);
                }
            }
        }
        pumpingLake.sort(new ChunkPositionSorter(getBlockPos().getX() + 0.5, getBlockPos().getY() - currentDepth - 1, getBlockPos().getZ() + 0.5, IBlockOrdered.Ordering.HIGH_TO_LOW));
        Collections.reverse(pumpingLake);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag))
            return;

        try {
            pumpMode = PumpMode.valueOf(tag);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public RedstoneController<TileEntityGasLift> getRedstoneController() {
        return rsController;
    }

    @Override
    public float getMinWorkingPressure() {
        return 0.5F + currentDepth * 0.05F;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("Items", inventory.serializeNBT());
        tag.putString("mode", pumpMode.toString());
        tag.putInt("currentDepth", currentDepth);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        if (tag.contains("mode")) pumpMode = PumpMode.valueOf(tag.getString("mode"));
        currentDepth = tag.getInt("currentDepth");
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventoryCap;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, fluidCap);
        } else {
            return super.getCapability(cap, facing);
        }
    }

    public IFluidTank getTank() {
        return tank;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ContainerGasLift(i, playerInventory, getBlockPos());
    }

    @Nonnull
    @Override
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", tank);
    }

    private class GasLiftFluidTank extends SmartSyncTank {
        GasLiftFluidTank() {
            super(TileEntityGasLift.this, PneumaticValues.NORMAL_TANK_CAPACITY);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            int inTank = fluid.getAmount();
            int amount = pumpMode == PumpMode.PUMP_LEAVE_FLUID ? Math.max(0, inTank - 1) : inTank;
            return super.drain(Math.min(maxDrain, amount), action);
        }
    }
}
