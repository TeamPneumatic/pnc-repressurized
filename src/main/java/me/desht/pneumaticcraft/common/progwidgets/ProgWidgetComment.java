package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;

public class ProgWidgetComment extends ProgWidgetText {
    public ProgWidgetComment() {
        super(ModProgWidgets.COMMENT);
    }

    @Override
    public ProgWidgetType returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(new TranslationTextComponent("gui.progWidget.comment.tooltip.freeToUse"));
    }

    @Override
    protected boolean addToTooltip() {
        return false;
    }

    @Override
    public int getHeight() {
        return super.getHeight() + 10;
    }

    @Override
    public int getWidth() {
        return super.getWidth() + 10;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_COMMENT;
    }

    @Override
    public String getExtraStringInfo() {
        return string;
    }

}
