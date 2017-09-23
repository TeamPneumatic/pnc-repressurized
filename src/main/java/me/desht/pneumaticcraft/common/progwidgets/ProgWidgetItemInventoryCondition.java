package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ProgWidgetItemInventoryCondition extends ProgWidgetCondition {

    @Override
    public String getWidgetString() {
        return "conditionItemInventory";
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class, ProgWidgetItemFilter.class, ProgWidgetString.class};
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {

            @Override
            protected boolean evaluate(BlockPos pos) {
                if (drone.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                    int count = 0;
                    IItemHandler handler = drone.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (widget.isItemValidForFilters(stack)) {
                            count += stack.getCount();
                        }
                    }
                    return ((ICondition) widget).getOperator() == ICondition.Operator.EQUALS ?
                            count == ((ICondition) widget).getRequiredCount() :
                            count >= ((ICondition) widget).getRequiredCount();
                }

                return false;
            }

        };
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_ITEM_INVENTORY;
    }

}
