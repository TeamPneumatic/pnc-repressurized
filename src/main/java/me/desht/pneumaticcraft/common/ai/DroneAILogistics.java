package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.ai.LogisticsManager.LogisticsTask;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.progwidgets.ILiquidExport;
import me.desht.pneumaticcraft.common.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.StreamUtils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
                AxisAlignedBB aabb = widget.getAreaExtents();
                Stream<ISemiBlock> semiBlocksInArea = SemiblockTracker.getInstance().getSemiblocksInArea(drone.world(), aabb);
                Stream<EntityLogisticsFrame> logisticFrames = StreamUtils.ofType(EntityLogisticsFrame.class, semiBlocksInArea);
                LogisticsManager manager = new LogisticsManager();
                logisticFrames.filter(frame -> area.contains(frame.getBlockPos())).forEach(manager::addLogisticFrame);
                drone.setLogisticsManager(manager);
            }
        }
        return drone.getLogisticsManager();
    }

    @Override
    public boolean shouldExecute() {
        if (getLogisticsManager() == null) return false;
        curTask = null;
        return doLogistics();
    }

    private boolean doLogistics() {
        ItemStack item = drone.getInv().getStackInSlot(0);
        FluidStack fluid = drone.getFluidTank().getFluid();
        PriorityQueue<LogisticsTask> tasks = getLogisticsManager().getTasks(item.isEmpty() ? fluid : item);
        if (tasks.size() > 0) {
            curTask = tasks.poll();
            return execute(curTask);
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (curTask == null) return false;
        if (!curAI.shouldContinueExecuting()) {
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
                    new FakeWidgetLogistics(task.requester.getBlockPos(), task.requester.getFacing(), task.transportingItem));
        } else if (drone.getFluidTank().getFluidAmount() > 0 && !task.transportingFluid.isEmpty()) {
            if (hasNoPathTo(task.requester.getBlockPos())) return false;
            curAI = new DroneAILiquidExport<>(drone,
                    new FakeWidgetLogistics(task.requester.getBlockPos(), task.requester.getFacing(), task.transportingFluid));
        } else if (!task.transportingItem.isEmpty()) {
            if (hasNoPathTo(task.provider.getBlockPos())) return false;
            curAI = new DroneEntityAIInventoryImport(drone,
                    new FakeWidgetLogistics(task.provider.getBlockPos(), task.provider.getFacing(), task.transportingItem));
        } else {
            if (hasNoPathTo(task.provider.getBlockPos())) return false;
            curAI = new DroneAILiquidImport<>(drone,
                    new FakeWidgetLogistics(task.provider.getBlockPos(),  task.provider.getFacing(), task.transportingFluid));
        }
        if (curAI.shouldExecute()) {
            task.informRequester();
            return true;
        } else {
            return false;
        }
    }

    private boolean hasNoPathTo(BlockPos pos) {
        for (Direction d : Direction.VALUES) {
            if (drone.isBlockValidPathfindBlock(pos.offset(d))) return false;
        }
        drone.addDebugEntry("pneumaticcraft.gui.progWidget.general.debug.cantNavigate", pos);
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
            sides[side.getIndex()] = true;
        }

        FakeWidgetLogistics(BlockPos pos, Direction side, FluidStack fluid) {
            super(ModProgWidgets.LOGISTICS.get());
            this.stack = ItemStack.EMPTY;
            this.fluid = fluid;
            area = new HashSet<>();
            area.add(pos);
            sides[side.getIndex()] = true;
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
            return !item.isEmpty() && item.isItemEqual(stack);
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
