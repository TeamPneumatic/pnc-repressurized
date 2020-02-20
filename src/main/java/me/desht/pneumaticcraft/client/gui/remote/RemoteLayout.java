package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.remote.actionwidget.*;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteLayout {

    private final List<ActionWidget> actionWidgets = new ArrayList<>();
    private static final Map<String, Class<? extends ActionWidget>> registeredWidgets = new HashMap<>();

    static {
        registerWidget(ActionWidgetCheckBox.class);
        registerWidget(ActionWidgetLabel.class);
        registerWidget(ActionWidgetButton.class);
        registerWidget(ActionWidgetDropdown.class);
    }

    private static void registerWidget(Class<? extends ActionWidget> widgetClass) {
        try {
            ActionWidget widget = widgetClass.newInstance();
            registeredWidgets.put(widget.getId(), widgetClass);
            return;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Widget " + widgetClass + " couldn't be registered");
    }

    public RemoteLayout(ItemStack remote, int guiLeft, int guiTop) {
        CompoundNBT tag = remote.getTag();
        if (tag != null) {
            ListNBT tagList = tag.getList("actionWidgets", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT widgetTag = tagList.getCompound(i);
                String id = widgetTag.getString("id");
                Class<? extends ActionWidget> clazz = registeredWidgets.get(id);
                try {
                    ActionWidget widget = clazz.newInstance();
                    widget.readFromNBT(widgetTag, guiLeft, guiTop);
                    actionWidgets.add(widget);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public CompoundNBT toNBT(int guiLeft, int guiTop) {
        CompoundNBT tag = new CompoundNBT();

        ListNBT tagList = new ListNBT();
        for (ActionWidget actionWidget : actionWidgets) {
            tagList.add(actionWidget.toNBT(guiLeft, guiTop));
        }
        tag.put("actionWidgets", tagList);
        return tag;
    }

    public void addWidget(ActionWidget widget) {
        actionWidgets.add(widget);
    }

    public List<ActionWidget> getActionWidgets() {
        return actionWidgets;
    }

    public List<Widget> getWidgets(boolean filterDisabledWidgets) {
        List<Widget> widgets = new ArrayList<>();
        for (ActionWidget actionWidget : actionWidgets) {
            if (!filterDisabledWidgets || actionWidget.isEnabled()) {
                widgets.add(actionWidget.getWidget());
            }
        }
        return widgets;
    }

}
