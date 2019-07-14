package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIImExBase;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class ProgWidgetDropItem extends ProgWidgetInventoryBase implements IItemDropper {
    private boolean dropStraight;

    @Override
    public String getWidgetString() {
        return "dropItem";
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.MAGENTA;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_DROP_ITEM;
    }

    @Override
    public boolean dropStraight() {
        return dropStraight;
    }

    @Override
    public void setDropStraight(boolean dropStraight) {
        this.dropStraight = dropStraight;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("dropStraight", dropStraight);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        dropStraight = tag.getBoolean("dropStraight");
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIImExBase<ProgWidgetDropItem>(drone, (ProgWidgetDropItem) widget) {

            private final Set<BlockPos> visitedPositions = new HashSet<>();

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
                        drone.world().addEntity(item);
                        if (useCount() && getRemainingCount() == 0) break;
                    }
                }
                return false;
            }

        };
    }

    @Override
    protected boolean isUsingSides() {
        return false;
    }

    @Override
    public String getExtraStringInfo() {
        return dropStraight() ? "Straight" : "Random";
    }
}
