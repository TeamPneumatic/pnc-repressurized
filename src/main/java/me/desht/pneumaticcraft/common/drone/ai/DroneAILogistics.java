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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.LogisticsManager;
import me.desht.pneumaticcraft.common.drone.LogisticsManager.LogisticsTask;
import me.desht.pneumaticcraft.common.drone.progwidgets.ILiquidExport;
import me.desht.pneumaticcraft.common.drone.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.StreamUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Stream;

public class DroneAILogistics extends Goal {
    private Goal curAI;
    private final IDroneBase drone;
    private final ProgWidgetAreaItemBase widget;
    private LogisticsTask curTask;

    public DroneAILogistics(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        this.drone = drone;
        this.widget = widget;
    }

    private LogisticsManager getLogisticsManager() {
        if (drone.getLogisticsManager() == null) {
            // note: this is an expensive operation!  hence we cache the logistics manager object in the drone
            Set<BlockPos> area = widget.getCachedAreaSet();
            if (!area.isEmpty()) {
                AABB aabb = widget.getAreaExtents();
                Stream<ISemiBlock> semiBlocksInArea = SemiblockTracker.getInstance().getSemiblocksInArea(drone.world(), aabb);
                Stream<AbstractLogisticsFrameEntity> logisticFrames = StreamUtils.ofType(AbstractLogisticsFrameEntity.class, semiBlocksInArea);
                LogisticsManager manager = new LogisticsManager();
                logisticFrames.filter(frame -> area.contains(frame.getBlockPos())).forEach(manager::addLogisticFrame);
                drone.setLogisticsManager(manager);
            }
        }
        return drone.getLogisticsManager();
    }

    @Override
    public boolean canUse() {
        if (getLogisticsManager() == null) return false;
        curTask = null;
        return doLogistics();
    }

    private boolean doLogistics() {
        ItemStack item = drone.getInv().getStackInSlot(0);
        FluidStack fluid = drone.getFluidTank().getFluid();
        PriorityQueue<LogisticsTask> tasks = getLogisticsManager().getTasks(item.isEmpty() ? fluid : item, true);
        if (tasks.size() > 0) {
            curTask = tasks.poll();
            return execute(curTask);
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (curTask == null) return false;
        if (!curAI.canContinueToUse()) {
            if (curAI instanceof DroneEntityAIInventoryImport) {
                curTask.requester.clearIncomingStack(curTask.transportingItem);
                return clearAIAndProvideAgain();
            } else if (curAI instanceof DroneAILiquidImport) {
                curTask.requester.clearIncomingStack(curTask.transportingFluid);
                return clearAIAndProvideAgain();
            } else {
                curAI = null;
                return false;
            }
        } else {
            curTask.informRequester();
            return true;
        }
    }

    private boolean clearAIAndProvideAgain() {
        curAI = null;
        if (curTask.isStillValid(drone.getInv().getStackInSlot(0).isEmpty() ? drone.getFluidTank().getFluid() : drone.getInv().getStackInSlot(0)) && execute(curTask)) {
            return true;
        } else {
            curTask = null;
            return doLogistics();
        }
    }

    public boolean execute(LogisticsTask task) {
        if (!drone.getInv().getStackInSlot(0).isEmpty() && !task.transportingItem.isEmpty()) {
            if (hasNoPathTo(task.requester.getBlockPos())) return false;
            curAI = new DroneEntityAIInventoryExport(drone,
                    new FakeWidgetLogistics(task.requester.getBlockPos(), task.requester.getSide(), task.transportingItem));
        } else if (drone.getFluidTank().getFluidAmount() > 0 && !task.transportingFluid.isEmpty()) {
            if (hasNoPathTo(task.requester.getBlockPos())) return false;
            curAI = new DroneAILiquidExport<>(drone,
                    new FakeWidgetLogistics(task.requester.getBlockPos(), task.requester.getSide(), task.transportingFluid));
        } else if (!task.transportingItem.isEmpty()) {
            if (hasNoPathTo(task.provider.getBlockPos())) return false;
            curAI = new DroneEntityAIInventoryImport(drone,
                    new FakeWidgetLogistics(task.provider.getBlockPos(), task.provider.getSide(), task.transportingItem));
        } else {
            if (hasNoPathTo(task.provider.getBlockPos())) return false;
            curAI = new DroneAILiquidImport<>(drone,
                    new FakeWidgetLogistics(task.provider.getBlockPos(),  task.provider.getSide(), task.transportingFluid));
        }
        if (curAI.canUse()) {
            task.informRequester();
            return true;
        } else {
            return false;
        }
    }

    private boolean hasNoPathTo(BlockPos pos) {
        for (Direction d : DirectionUtil.VALUES) {
            if (drone.isBlockValidPathfindBlock(pos.relative(d))) return false;
        }
        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.general.debug.cantNavigate", pos);
        return true;
    }

    private static class FakeWidgetLogistics extends ProgWidgetInventoryBase implements ILiquidFiltered, ILiquidExport {
        private final ItemStack stack;
        private final FluidStack fluid;
        private final Set<BlockPos> area;
        private final boolean[] sides = new boolean[6];

        FakeWidgetLogistics(BlockPos pos, Direction side, @Nonnull ItemStack stack) {
            super(ModProgWidgets.LOGISTICS.get());
            this.stack = stack;
            this.fluid = FluidStack.EMPTY;
            area = new HashSet<>();
            area.add(pos);
            sides[side.get3DDataValue()] = true;
        }

        FakeWidgetLogistics(BlockPos pos, Direction side, FluidStack fluid) {
            super(ModProgWidgets.LOGISTICS.get());
            this.stack = ItemStack.EMPTY;
            this.fluid = fluid;
            area = new HashSet<>();
            area.add(pos);
            sides[side.get3DDataValue()] = true;
        }

        @Override
        public DyeColor getColor() {
            return DyeColor.WHITE;  // arbitrary
        }

        @Override
        public void getArea(Set<BlockPos> area) {
            area.addAll(this.area);
        }

        @Override
        public void setSides(boolean[] sides) {
        }

        @Override
        public boolean[] getSides() {
            return sides;
        }

        @Override
        public boolean isItemValidForFilters(@Nonnull ItemStack item) {
            return !item.isEmpty() && ItemStack.isSameItem(item, stack);
        }

        @Override
        public ResourceLocation getTexture() {
            return null;
        }

        @Override
        public boolean useCount() {
            return true;
        }

        @Override
        public void setUseCount(boolean useCount) {
        }

        @Override
        public int getCount() {
            return !stack.isEmpty() ? stack.getCount() : fluid.getAmount();
        }

        @Override
        public void setCount(int count) {
        }

        @Override
        public boolean isFluidValid(Fluid fluid) {
            return fluid == this.fluid.getFluid();
        }

        @Override
        public void setPlaceFluidBlocks(boolean placeFluidBlocks) {
        }

        @Override
        public boolean isPlacingFluidBlocks() {
            return false;
        }
    }

}
