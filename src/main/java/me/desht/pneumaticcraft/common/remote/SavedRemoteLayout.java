package me.desht.pneumaticcraft.common.remote;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.remote.IRemoteWidget;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SavedRemoteLayout {
    public static final int JSON_VERSION = 2;

    public static final Codec<SavedRemoteLayout> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IRemoteWidget.CODEC.listOf().fieldOf("widgets").forGetter(s -> s.widgets)
    ).apply(builder, SavedRemoteLayout::new));

    public static final Codec<Versioned> VERSIONED_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("version").forGetter(Versioned::version),
            IRemoteWidget.CODEC.listOf().fieldOf("widgets").forGetter(Versioned::widgets)
    ).apply(builder, Versioned::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SavedRemoteLayout> STREAM_CODEC = StreamCodec.composite(
            IRemoteWidget.STREAM_CODEC.apply(ByteBufCodecs.list()), s -> s.widgets,
            SavedRemoteLayout::new
    );

    public static final SavedRemoteLayout EMPTY = new SavedRemoteLayout(List.of());

    private final List<IRemoteWidget> widgets;  // note: immutable!

    public SavedRemoteLayout(Collection<IRemoteWidget> widgets) {
        this.widgets = List.copyOf(widgets);
    }

    public static SavedRemoteLayout fromJson(HolderLookup.Provider provider, JsonElement json) {
        RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
        return VERSIONED_CODEC.parse(ops, json)
                .resultOrPartial(err -> Log.warning("can't parse remote layout JSON: " + err))
                .map(v -> new SavedRemoteLayout(v.widgets()))
                .orElse(EMPTY);
    }

    public JsonElement toJson(HolderLookup.Provider provider) {
        RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
        SavedRemoteLayout.Versioned saved = new SavedRemoteLayout.Versioned(JSON_VERSION, widgets);

        return SavedRemoteLayout.VERSIONED_CODEC.encodeStart(ops, saved).result().orElseThrow();
    }

    public static SavedRemoteLayout fromItem(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.REMOTE_LAYOUT, SavedRemoteLayout.EMPTY);
    }

    public List<IRemoteWidget> getWidgets() {
        return widgets;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SavedRemoteLayout) obj;
        return Objects.equals(this.widgets, that.widgets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(widgets);
    }

    @Override
    public String toString() {
        return "Saved[" + "widgets=" + widgets + ']';
    }

    public record Versioned(int version, List<IRemoteWidget> widgets) {
    }
}
