package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIImExBase;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgWidgetDropItem extends ProgWidgetInventoryBase implements IItemDropper {
    private boolean dropStraight;
    private boolean pickupDelay = true;

    public ProgWidgetDropItem() {
        super(ModProgWidgets.DROP_ITEM);
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

    public boolean isPickupDelay() {
        return pickupDelay;
    }

    public void setPickupDelay(boolean pickupDelay) {
        this.pickupDelay = pickupDelay;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("dropStraight", dropStraight);
        tag.putBoolean("pickupDelay", pickupDelay);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        dropStraight = tag.getBoolean("dropStraight");
        pickupDelay = tag.getBoolean("pickupDelay");
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(dropStraight);
        buf.writeBoolean(pickupDelay);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        dropStraight = buf.readBoolean();
        pickupDelay = buf.readBoolean();
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        if (pickupDelay) {
            curTooltip.add(new TranslationTextComponent("gui.progWidget.drop.hasPickupDelay"));
        } else {
            curTooltip.add(new TranslationTextComponent("gui.progWidget.drop.noPickupDelay"));
        }
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
                        if (pickupDelay) item.setPickupDelay(40);
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
        return I18n.format("gui.progWidget.drop.dropMethod." + (dropStraight() ? "straight" : "random"));
    }
}
