package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ProgWidgetStart extends ProgWidget {

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
        return null;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_START;
    }

    @Override
    public String getWidgetString() {
        return "start";
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.LIME;
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        for (IProgWidget widget : widgets) {
            if (widget != this && widget instanceof ProgWidgetStart) {
                curInfo.add("gui.progWidget.general.error.multipleStartPieces");
                break;
            }
        }
    }
}
