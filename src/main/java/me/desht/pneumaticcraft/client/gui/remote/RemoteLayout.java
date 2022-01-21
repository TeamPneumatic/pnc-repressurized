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

package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.remote.actionwidget.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteLayout {

    private final List<ActionWidget<?>> actionWidgets = new ArrayList<>();
    private static final Map<String, Class<? extends ActionWidget<?>>> registeredWidgets = new HashMap<>();

    // FIXME don't need reflection shenanigans here

    static {
        registerWidget(ActionWidgetCheckBox.class);
        registerWidget(ActionWidgetLabel.class);
        registerWidget(ActionWidgetButton.class);
        registerWidget(ActionWidgetDropdown.class);
    }

    private static void registerWidget(Class<? extends ActionWidget<?>> widgetClass) {
        try {
            ActionWidget<?> widget = widgetClass.getDeclaredConstructor().newInstance();
            registeredWidgets.put(widget.getId(), widgetClass);
            return;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Widget " + widgetClass + " couldn't be registered");
    }

    public RemoteLayout(ItemStack remote, int guiLeft, int guiTop) {
        CompoundTag tag = remote.getTag();
        if (tag != null) {
            ListTag tagList = tag.getList("actionWidgets", Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundTag widgetTag = tagList.getCompound(i);
                String id = widgetTag.getString("id");
                Class<? extends ActionWidget<?>> clazz = registeredWidgets.get(id);
                try {
                    ActionWidget<?> widget = clazz.newInstance();
                    widget.readFromNBT(widgetTag, guiLeft, guiTop);
                    actionWidgets.add(widget);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public CompoundTag toNBT(int guiLeft, int guiTop) {
        CompoundTag tag = new CompoundTag();

        ListTag tagList = new ListTag();
        for (ActionWidget<?> actionWidget : actionWidgets) {
            tagList.add(actionWidget.toNBT(guiLeft, guiTop));
        }
        tag.put("actionWidgets", tagList);
        return tag;
    }

    public void addWidget(ActionWidget<?> widget) {
        actionWidgets.add(widget);
    }

    public List<ActionWidget<?>> getActionWidgets() {
        return actionWidgets;
    }

    public List<AbstractWidget> getWidgets(boolean filterDisabledWidgets) {
        List<AbstractWidget> widgets = new ArrayList<>();
        for (ActionWidget<?> actionWidget : actionWidgets) {
            if (!filterDisabledWidgets || actionWidget.isEnabled()) {
                widgets.add(actionWidget.getWidget());
            }
        }
        return widgets;
    }

}
