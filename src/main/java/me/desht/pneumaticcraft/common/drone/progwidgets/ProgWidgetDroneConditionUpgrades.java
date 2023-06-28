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

package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
        int count = PneumaticRegistry.getInstance().getUpgradeRegistry().getKnownUpgrades().stream()
                .filter(upgrade -> drone.getUpgrades(upgrade) > 0)
                .filter(upgrade -> widget instanceof IItemFiltering f && f.isItemValidForFilters(upgrade.getItemStack()))
                .mapToInt(drone::getUpgrades)
                .sum();

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
                getConnectedWidgetList(this, 0, ModProgWidgets.ITEM_FILTER.get()),
                getConnectedWidgetList(this, getParameters().size(), ModProgWidgets.ITEM_FILTER.get()),
                null);
    }

}
