package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IItemDropper;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class DroneAIDropItem<W extends ProgWidgetInventoryBase & IItemDropper> extends DroneAIImExBase<W> {
    private final Set<BlockPos> visitedPositions = new HashSet<>();

    public DroneAIDropItem(IDroneBase drone, W widget) {
        super(drone, widget);
    }

    @Override
    public boolean shouldExecute() {
        boolean shouldExecute = false;
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            if (progWidget.isItemValidForFilters(stack)) {
                shouldExecute = super.shouldExecute();
                break;
            }
        }
        return shouldExecute;
    }

    @Override
    protected boolean moveIntoBlock() {
        return true;
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return !visitedPositions.contains(pos);//another requirement is that the drone can navigate to this exact block, but that's handled by the pathfinder.
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        visitedPositions.add(pos);
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            if (progWidget.isItemValidForFilters(stack)) {
                if (useCount() && getRemainingCount() < stack.getCount()) {
                    stack = stack.split(getRemainingCount());
                    decreaseCount(getRemainingCount());
                } else {
                    decreaseCount(stack.getCount());
                    drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                }
                ItemEntity item = new ItemEntity(drone.world(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                if (progWidget.dropStraight()) {
                    item.setMotion(0, 0, 0);
                }
                if (progWidget.hasPickupDelay()) {
                    item.setPickupDelay(40);
                }
                drone.world().addEntity(item);
                if (useCount() && getRemainingCount() == 0) break;
            }
        }
        return false;
    }
}
