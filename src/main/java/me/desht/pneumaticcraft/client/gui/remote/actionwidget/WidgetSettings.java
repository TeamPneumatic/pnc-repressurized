package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.client.gui.RemoteEditorScreen;
import me.desht.pneumaticcraft.mixin.accessors.TooltipAccess;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.Objects;

public final class WidgetSettings {
    public static final Codec<WidgetSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("x").forGetter(WidgetSettings::getX),
            Codec.INT.fieldOf("y").forGetter(WidgetSettings::getY),
            Codec.INT.fieldOf("width").forGetter(WidgetSettings::getWidth),
            Codec.INT.fieldOf("height").forGetter(WidgetSettings::getHeight),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(WidgetSettings::getTitle),
            ComponentSerialization.CODEC.fieldOf("tooltip").forGetter(WidgetSettings::getTooltip)
    ).apply(builder, WidgetSettings::new));

    private int x;
    private int y;
    private int width;
    private int height;
    private Component title;
    private Component tooltip;

    public WidgetSettings(int x, int y, int width, int height, Component title, Component tooltip) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public static WidgetSettings forWidget(RemoteEditorScreen screen, AbstractWidget widget) {
        return new WidgetSettings(widget.getX() - screen.getGuiLeft(), widget.getY() - screen.getGuiTop(), widget.getWidth(), widget.getHeight(), widget.getMessage(), getTooltip(widget));
    }

    private static Component getTooltip(AbstractWidget widget) {
        return widget.getTooltip() == null ? Component.empty() : ((TooltipAccess) widget.getTooltip()).getMessage();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Component getTitle() {
        return title;
    }

    public void setTitle(Component title) {
        this.title = title;
    }

    public Component getTooltip() {
        return tooltip;
    }

    public void setTooltip(Component tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WidgetSettings) obj;
        return this.x == that.x &&
                this.y == that.y &&
                this.width == that.width &&
                this.height == that.height &&
                Objects.equals(this.title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height, title);
    }

    @Override
    public String toString() {
        return "WidgetSettings[" +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "width=" + width + ", " +
                "height=" + height + ", " +
                "title=" + title + ']';
    }

}
