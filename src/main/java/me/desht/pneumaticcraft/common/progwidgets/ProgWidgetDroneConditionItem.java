package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetDroneConditionItem extends ProgWidgetDroneEvaluation implements IItemFiltering {

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetItemFilter.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString() {
        return "droneConditionItem";
    }

    @Override
    protected int getCount(IDroneBase drone, IProgWidget widget) {
        int count = 0;
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack droneStack = drone.getInv().getStackInSlot(i);
            if (((IItemFiltering) widget).isItemValidForFilters(droneStack)) {
                count += droneStack.getCount();
            }
        }
        return count;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DRONE_ITEM_INVENTORY;
    }

    @Override
    public boolean isItemValidForFilters(ItemStack item) {
        return ProgWidgetItemFilter.isItemValidForFilters(item, ProgWidget.getConnectedWidgetList(this, 0), ProgWidget.getConnectedWidgetList(this, getParameters().length), null);
    }

}
