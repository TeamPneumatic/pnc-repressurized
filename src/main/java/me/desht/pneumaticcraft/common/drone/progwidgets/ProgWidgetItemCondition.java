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
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetItemCondition extends ProgWidgetConditionBase {

    public ProgWidgetItemCondition() {
        super(ModProgWidgets.CONDITION_ITEM.get());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.ITEM_FILTER.get(), ModProgWidgets.ITEM_FILTER.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null && getConnectedParameters()[3] == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.conditionItem.error.noCheckingItem"));
        }
        if (getConnectedParameters()[1] == null && getConnectedParameters()[4] == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.conditionItem.error.noFilter"));
        }
    }

    @Override
    public boolean evaluate(IDroneBase drone, IProgWidget widget) {
        ProgWidgetItemFilter checkedFilter = (ProgWidgetItemFilter) widget.getConnectedParameters()[0];
        while (checkedFilter != null) {
            if (!ProgWidgetItemFilter.isItemValidForFilters(checkedFilter.getFilter(),
                    getConnectedWidgetList(this, 1, ModProgWidgets.ITEM_FILTER.get()),
                    getConnectedWidgetList(this, getParameters().size() + 1, ModProgWidgets.ITEM_FILTER.get()),
                    null))
                return false;
            checkedFilter = (ProgWidgetItemFilter) checkedFilter.getConnectedParameters()[0];
        }

        checkedFilter = (ProgWidgetItemFilter) widget.getConnectedParameters()[3];
        while (checkedFilter != null) {
            if (ProgWidgetItemFilter.isItemValidForFilters(checkedFilter.getFilter(),
                    getConnectedWidgetList(this, 1, ModProgWidgets.ITEM_FILTER.get()),
                    getConnectedWidgetList(this, getParameters().size() + 1, ModProgWidgets.ITEM_FILTER.get()),
                    null))
                return false;
            checkedFilter = (ProgWidgetItemFilter) checkedFilter.getConnectedParameters()[0];
        }
        return true;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_ITEM;
    }

}
