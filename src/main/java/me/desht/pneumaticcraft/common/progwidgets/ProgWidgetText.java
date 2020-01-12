package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class ProgWidgetText extends ProgWidget {
    public String string = "";

    public ProgWidgetText() {
        super(ModProgWidgets.TEXT.get());
    }

    public ProgWidgetText(ProgWidgetType<?> type) {
        super(type);
    }

    public static ProgWidgetText withText(String string){
        ProgWidgetText widget = new ProgWidgetText();
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
    public ProgWidgetType returnType() {
        return ModProgWidgets.TEXT.get();
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return ImmutableList.of(ModProgWidgets.TEXT.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_TEXT;
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
