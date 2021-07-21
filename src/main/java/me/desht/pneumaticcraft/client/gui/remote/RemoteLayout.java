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
import java.util.function.Supplier;

public class RemoteLayout {
    private final List<ActionWidget<?>> actionWidgets = new ArrayList<>();
    private static final Map<String, Supplier<ActionWidget<?>>> registeredWidgets = new HashMap<>();

    static {
        registerWidget(ActionWidgetCheckBox::new);
        registerWidget(ActionWidgetLabel::new);
        registerWidget(ActionWidgetButton::new);
        registerWidget(ActionWidgetDropdown::new);
    }

    private static void registerWidget(Supplier<ActionWidget<?>> supplier) {
        ActionWidget<?> widget = supplier.get();
        registeredWidgets.put(widget.getId(), supplier);
    }

    public RemoteLayout(ItemStack remote, int guiLeft, int guiTop) {
        CompoundNBT tag = remote.getTag();
        if (tag != null) {
            ListNBT tagList = tag.getList("actionWidgets", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT widgetTag = tagList.getCompound(i);
                String id = widgetTag.getString("id");
                Supplier<ActionWidget<?>> sup = registeredWidgets.get(id);
                if (sup != null) {
                    ActionWidget<?> actionWidget = sup.get();
                    actionWidget.readFromNBT(widgetTag, guiLeft, guiTop);
                    actionWidgets.add(actionWidget);
                }
            }
        }
    }

    public CompoundNBT toNBT(int guiLeft, int guiTop) {
        CompoundNBT tag = new CompoundNBT();

        ListNBT tagList = new ListNBT();
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

    public List<Widget> getWidgets(boolean filterDisabledWidgets) {
        List<Widget> widgets = new ArrayList<>();
        for (ActionWidget<?> actionWidget : actionWidgets) {
            if (!filterDisabledWidgets || actionWidget.isEnabled()) {
                widgets.add(actionWidget.getWidget());
            }
        }
        return widgets;
    }

}
