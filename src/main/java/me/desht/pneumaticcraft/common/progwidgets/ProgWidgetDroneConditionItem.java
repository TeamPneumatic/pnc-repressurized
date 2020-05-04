package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ProgWidgetDroneConditionItem extends ProgWidgetDroneCondition implements IItemFiltering {

    public ProgWidgetDroneConditionItem() {
        super(ModProgWidgets.DRONE_CONDITION_ITEM);
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.ITEM_FILTER, ModProgWidgets.TEXT);
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
        return ProgWidgetItemFilter.isItemValidForFilters(item,
                ProgWidget.getConnectedWidgetList(this, 0, ModProgWidgets.ITEM_FILTER),
                ProgWidget.getConnectedWidgetList(this, getParameters().size(), ModProgWidgets.ITEM_FILTER),
                null);
    }

}
