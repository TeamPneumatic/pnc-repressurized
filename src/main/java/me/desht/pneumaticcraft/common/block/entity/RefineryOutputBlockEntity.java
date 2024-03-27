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
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.inventory.RefineryMenu;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class RefineryOutputBlockEntity extends AbstractTickingBlockEntity implements
        IRedstoneControl<RefineryOutputBlockEntity>, IComparatorSupport, ISerializableTanks,
        MenuProvider, IHeatExchangingTE {

    private RefineryControllerBlockEntity controllerTE = null;

    @DescSynced
    private final SmartSyncTank outputTank = new SmartSyncTank(this, PneumaticValues.NORMAL_TANK_CAPACITY);

    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();

    private final IFluidHandler wrappedTank = new TankWrapper(outputTank);
    private final RedstoneController<RefineryOutputBlockEntity> rsController = new RedstoneController<>(this);

    public RefineryOutputBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.REFINERY_OUTPUT.get(), pos, state);
    }

    @Override
    public boolean hasFluidCapability() {
        return true;
    }

    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction dir) {
        return dir == Direction.DOWN ? outputTank : wrappedTank;
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        outputTank.tick();
    }

    @Override
    public int getComparatorValue() {
        RefineryControllerBlockEntity controller = getRefineryController();
        return controller == null ? 0 : controller.getComparatorValue();
    }

    @Override
    public RedstoneController<RefineryOutputBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public int getRedstoneMode() {
        RefineryControllerBlockEntity controller = getRefineryController();
        return controller == null ? 0 : controller.getRedstoneMode();
    }

    @Nonnull
    @Override
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", outputTank);
    }

    @Override
    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return null;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        RefineryControllerBlockEntity controller = getRefineryController();
        return controller == null ? null : new RefineryMenu(windowId, inventory, controller.getBlockPos());
    }

    public RefineryControllerBlockEntity getRefineryController() {
        if (controllerTE != null && controllerTE.isRemoved()) controllerTE = null;

        if (controllerTE == null) {
            Level level = nonNullLevel();
            BlockPos checkPos = this.worldPosition;
            while (level.getBlockState(checkPos.below()).getBlock() == ModBlocks.REFINERY_OUTPUT.get()) {
                checkPos = checkPos.below();
            }
            if (level.getBlockState(checkPos.below()).getBlock() == ModBlocks.REFINERY.get()) {
                // refinery directly under the output stack
                controllerTE = (RefineryControllerBlockEntity) level.getBlockEntity(checkPos.below());
            } else {
                // is refinery horizontally adjacent to bottom of stack?
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockState(checkPos.relative(d)).getBlock() == ModBlocks.REFINERY.get()) {
                        controllerTE = (RefineryControllerBlockEntity) level.getBlockEntity(checkPos.relative(d));
                    }
                }
            }
        }
        return controllerTE;
    }

    public IFluidTank getOutputTank() {
        return outputTank;
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }

    private record TankWrapper(SmartSyncTank wrapped) implements IFluidHandler {
        @Override
        public int getTanks() {
            return wrapped.getTanks();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return wrapped.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return wrapped.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return wrapped.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;  // this tank is only for draining
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return wrapped.drain(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return wrapped.drain(maxDrain, action);
        }
    }
}
