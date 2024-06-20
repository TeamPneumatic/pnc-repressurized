package me.desht.pneumaticcraft.common.drone.progwidgets;

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class WidgetSerializer {
    public static void writeProgWidgetsToPacket(List<IProgWidget> widgetList, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(widgetList.size());
        for (IProgWidget progWidget : widgetList) {
            progWidget.writeToPacket(buf);
        }
    }

    public static List<IProgWidget> readWidgetsFromPacket(RegistryFriendlyByteBuf buf) {
        List<IProgWidget> widgets = new ArrayList<>();
        int nWidgets = buf.readVarInt();
        for (int i = 0; i < nWidgets; i++) {
            try {
                IProgWidget widget = ProgWidget.fromPacket(buf);
                if (!widget.isAvailable()) {
                    Log.warning("ignoring unavailable widget type " + widget.getTypeID().toString());
                } else {
                    widgets.add(widget);
                }
            } catch (IllegalStateException e) {
                Log.warning(e.getMessage());
            }
        }
        return widgets;
    }

    public static List<IProgWidget> getWidgetsFromNBT(CompoundTag tag) {
        List<IProgWidget> newWidgets = new ArrayList<>();
        ListTag widgetTags = tag.getList(IProgrammable.NBT_WIDGETS, Tag.TAG_COMPOUND);
        for (int i = 0; i < widgetTags.size(); i++) {
            IProgWidget addedWidget = ProgWidget.fromNBT(widgetTags.getCompound(i));
            if (addedWidget != null) {
                if (addedWidget.isAvailable()) {
                    newWidgets.add(addedWidget);
                } else {
                    Log.warning("ignoring unavailable widget type: " + addedWidget.getType());
                }
            }
        }
        return newWidgets;
    }

    public static void putWidgetsToNBT(List<IProgWidget> widgets, CompoundTag tag) {
        tag.put(IProgrammable.NBT_WIDGETS, Util.make(new ListTag(), list ->
                widgets.forEach(widget -> ProgWidget.CODEC.encodeStart(NbtOps.INSTANCE, widget).ifSuccess(list::add))));
    }
}
