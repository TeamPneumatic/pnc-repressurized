package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetLabel extends ProgWidget implements ILabel {

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) curInfo.add(xlate("gui.progWidget.label.error.noLabel"));
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public boolean hasStepOutput() {
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public String getWidgetString() {
        return "label";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_LABEL;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.MEDIUM;
    }

    @Override
    public String getLabel() {
        ProgWidgetString labelWidget = (ProgWidgetString) getConnectedParameters()[0];
        return labelWidget != null ? labelWidget.string : null;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }
}
