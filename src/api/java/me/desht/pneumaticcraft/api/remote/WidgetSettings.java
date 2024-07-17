package me.desht.pneumaticcraft.api.remote;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record WidgetSettings(int x, int y, int width, int height, Component title, Component tooltip) {
    public static final Codec<WidgetSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("x").forGetter(WidgetSettings::x),
            Codec.INT.fieldOf("y").forGetter(WidgetSettings::y),
            Codec.INT.optionalFieldOf("width", 0).forGetter(WidgetSettings::width),
            Codec.INT.optionalFieldOf("height", 0).forGetter(WidgetSettings::height),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(WidgetSettings::title),
            ComponentSerialization.CODEC.optionalFieldOf("tooltip", Component.empty()).forGetter(WidgetSettings::tooltip)
    ).apply(builder, WidgetSettings::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, WidgetSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, WidgetSettings::x,
            ByteBufCodecs.INT, WidgetSettings::y,
            ByteBufCodecs.VAR_INT, WidgetSettings::width,
            ByteBufCodecs.VAR_INT, WidgetSettings::height,
            ComponentSerialization.STREAM_CODEC, WidgetSettings::title,
            ComponentSerialization.STREAM_CODEC, WidgetSettings::tooltip,
            WidgetSettings::new
    );

    public WidgetSettings(int x, int y, int width, int height, @NotNull Component title, @NotNull Component tooltip) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        this.tooltip = tooltip;
    }

    public WidgetSettings copy() {
        return new WidgetSettings(x, y, width, height, title.copy(), tooltip.copy());
    }

    public WidgetSettings copyToPos(int newX, int newY) {
        return new WidgetSettings(newX, newY, width, height, title.copy(), tooltip.copy());
    }

    public WidgetSettings resize(int newWidth, int newHeight) {
        return new WidgetSettings(x, y, newWidth, newHeight, title.copy(), tooltip.copy());
    }

    public WidgetSettings withText(Component title, Component tooltip) {
        return new WidgetSettings(x, y, width, height, title, tooltip);
    }
}
