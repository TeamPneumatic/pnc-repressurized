package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class PNCWidget<T extends AbstractWidget> extends AbstractWidget {
    public PNCWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
    }

    public T setTooltipText(Component comp) {
        setTooltip(Tooltip.create(comp));
        return (T) this;
    }

    public T setTooltipText(List<Component> comps) {
        setTooltip(Tooltip.create(PneumaticCraftUtils.combineComponents(comps)));
        return (T) this;
    }

    public T setTooltipKey(String tip) {
        setTooltip(Tooltip.create(xlate(tip)));
        return (T) this;
    }
}
