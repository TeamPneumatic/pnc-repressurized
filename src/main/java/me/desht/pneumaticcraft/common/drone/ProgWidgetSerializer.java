package me.desht.pneumaticcraft.common.drone;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidget;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.List;

public class ProgWidgetSerializer {
    public static List<IProgWidget> getWidgetsFromNBT(HolderLookup.Provider provider, Tag tag) {
        return filterAvailable(ProgWidget.LIST_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(message -> Log.warning("deserialization error: {}", message))
                .orElse(List.of())
        );
    }

    public static List<IProgWidget> getWidgetsFromJson(HolderLookup.Provider provider, JsonElement json) {
        return filterAvailable(ProgWidget.VERSIONED_SAVE_CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), json)
                .resultOrPartial(message -> Log.warning("deserialization error: {}", message))
                .map(ProgWidget.Versioned::widgets)
                .orElse(List.of())
        );
    }

    private static List<IProgWidget> filterAvailable(List<IProgWidget> list) {
        ImmutableList.Builder<IProgWidget> b = ImmutableList.builder();
        for (IProgWidget w : list) {
            if (w.isAvailable()) {
                b.add(w);
            } else {
                Log.warning("ignoring unavailable widget type: {}" + w.getType());
            }
        }
        return b.build();
    }

    public static Tag putWidgetsToNBT(HolderLookup.Provider provider, List<IProgWidget> widgets) {
        return ProgWidget.LIST_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), widgets)
                .getOrThrow();
    }

    public static JsonElement putWidgetsToJson(HolderLookup.Provider provider, List<IProgWidget> widgets) {
        return ProgWidget.VERSIONED_SAVE_CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE),
                        new ProgWidget.Versioned(ProgWidget.JSON_VERSION, widgets))
                .getOrThrow();
    }
}
