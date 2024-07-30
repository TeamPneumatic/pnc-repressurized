package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import java.util.function.Consumer;

public class PNCForgeSlider extends ExtendedSlider {
    private final Consumer<PNCForgeSlider> onApply;

    public PNCForgeSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString, Consumer<PNCForgeSlider> onApply) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize, precision, drawString);
        this.onApply = onApply;
    }

    public PNCForgeSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString, Consumer<PNCForgeSlider> onApply) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, drawString);
        this.onApply = onApply;
    }

    @Override
    protected void applyValue() {
        if (onApply != null) onApply.accept(this);
    }
}
