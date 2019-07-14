package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class ProgWidgetString extends ProgWidget {
    public String string = "";

    public static ProgWidgetString withText(String string){
        ProgWidgetString widget = new ProgWidgetString();
        widget.string = string;
        return widget;
    }
    
    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        if (addToTooltip()) curTooltip.add(new StringTextComponent("Value: \"" + string + "\""));
    }

    protected boolean addToTooltip() {
        return true;
    }

    @Override
    public String getExtraStringInfo() {
        return "\"" + string + "\"";
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return ProgWidgetString.class;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_TEXT;
    }

    @Override
    public String getWidgetString() {
        return "text";
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putString("string", string);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        string = tag.getString("string");
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.LIGHT_BLUE;
    }
}
