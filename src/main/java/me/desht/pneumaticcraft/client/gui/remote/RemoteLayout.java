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

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.client.gui.RemoteScreen;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidget;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidgets;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteLayout {
    public static final int JSON_VERSION = 3;

    public static final Codec<RemoteLayout.Versioned> VERSIONED_SAVE_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("version").forGetter(RemoteLayout.Versioned::version),
            ActionWidgets.LIST_CODEC.fieldOf("action_widgets").forGetter(RemoteLayout.Versioned::widgets)
    ).apply(builder, RemoteLayout.Versioned::new));

    // Note: this is a mutable list
    private final List<ActionWidget<?>> actionWidgets;

    private RemoteLayout(List<ActionWidget<?>> list) {
        actionWidgets = new ArrayList<>(list);
    }

    public static RemoteLayout createEmpty() {
        return new RemoteLayout(List.of());
    }

    public static RemoteLayout fromNBT(HolderLookup.Provider provider, CompoundTag tag) {
        RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
        var saved = Saved.CODEC.parse(ops, tag)
                .resultOrPartial(err -> Log.warning("can't parse remote layout NBT: " + err))
                .orElse(Saved.EMPTY);
        return new RemoteLayout(saved.widgets());
    }

    public static RemoteLayout fromJson(HolderLookup.Provider provider, JsonElement json) {
        RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
        return VERSIONED_SAVE_CODEC.parse(ops, json)
                .resultOrPartial(err -> Log.warning("can't parse remote layout JSON: " + err))
                .map(v -> new RemoteLayout(v.widgets))
                .orElse(createEmpty());
    }

    public CompoundTag toNBT(HolderLookup.Provider provider) {
        RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
        Tag tag = Saved.CODEC.encodeStart(ops, new Saved(actionWidgets)).result().orElseThrow();
        return tag instanceof CompoundTag c ? c : new CompoundTag();
    }

    public JsonElement toJson(HolderLookup.Provider provider) {
        RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
        return VERSIONED_SAVE_CODEC.encodeStart(ops, new Versioned(JSON_VERSION, actionWidgets)).result().orElseThrow();
    }

    public void addActionWidget(ActionWidget<?> actionWidget) {
        actionWidgets.add(actionWidget);
    }

    public void removeActionWidget(ActionWidget<?> actionWidget) {
        actionWidgets.remove(actionWidget);
    }

    public List<ActionWidget<?>> getActionWidgets() {
        return Collections.unmodifiableList(actionWidgets);
    }

    public List<AbstractWidget> getOrCreateMinecraftWidgets(RemoteScreen screen, boolean filterDisabledWidgets) {
        return actionWidgets.stream()
                .filter(actionWidget -> !filterDisabledWidgets || actionWidget.isEnabled())
                .map(actionWidget -> actionWidget.getOrCreateMinecraftWidget(screen))
                .collect(Collectors.toList());
    }

    public record Saved(List<ActionWidget<?>> widgets) {
        public static final Codec<Saved> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ActionWidgets.CODEC.listOf().fieldOf("widgets").forGetter(Saved::widgets)
        ).apply(builder, Saved::new));

        public static Saved EMPTY = new Saved(List.of());
    }

    public record Versioned(int version, List<ActionWidget<?>> widgets) {
    }
}
