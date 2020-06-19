package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetStart extends ProgWidget {

    public ProgWidgetStart() {
        super(ModProgWidgets.START);
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
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_START;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.LIME;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        for (IProgWidget widget : widgets) {
            if (widget != this && widget instanceof ProgWidgetStart) {
                curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.multipleStartPieces"));
                break;
            }
        }
    }
}
