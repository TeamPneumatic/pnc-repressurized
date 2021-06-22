package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ProgWidgetDroneConditionUpgrades extends ProgWidgetDroneCondition implements IItemFiltering {
    public ProgWidgetDroneConditionUpgrades() {
        super(ModProgWidgets.DRONE_CONDITION_UPGRADES.get());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.ITEM_FILTER.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    protected int getCount(IDroneBase drone, IProgWidget widget) {
        int count = 0;

        for (EnumUpgrade upgrade : EnumUpgrade.values()) {
            if (drone.getUpgrades(upgrade) > 0) {
                if (((IItemFiltering) widget).isItemValidForFilters(upgrade.getItemStack())) {
                    count += drone.getUpgrades(upgrade);
                }
            }
        }

        maybeRecordMeasuredVal(drone, count);
        return count;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DRONE_UPGRADES;
    }

    @Override
    public boolean isItemValidForFilters(ItemStack item) {
        return ProgWidgetItemFilter.isItemValidForFilters(item,
                ProgWidget.getConnectedWidgetList(this, 0, ModProgWidgets.ITEM_FILTER.get()),
                ProgWidget.getConnectedWidgetList(this, getParameters().size(), ModProgWidgets.ITEM_FILTER.get()),
                null);
    }

}
