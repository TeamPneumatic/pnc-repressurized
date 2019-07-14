package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneEntityAIGoToLocation;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetGoToLocation extends ProgWidget implements IGotoWidget, IAreaProvider {

    public boolean doneWhenDeparting;

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) {
            curInfo.add(xlate("gui.progWidget.area.error.noArea"));
        }
    }

    @Override
    public boolean doneWhenDeparting() {
        return doneWhenDeparting;
    }

    @Override
    public void setDoneWhenDeparting(boolean bool) {
        doneWhenDeparting = bool;
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(new StringTextComponent("Done when " + (doneWhenDeparting ? "departing" : "arrived")));
    }

    @Override
    public String getWidgetString() {
        return "goto";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_GOTO;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneEntityAIGoToLocation(drone, (ProgWidget) widget);
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    public void getArea(Set<BlockPos> area) {
        ProgWidgetAreaItemBase.getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[getParameters().length]);
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("doneWhenDeparting", doneWhenDeparting);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        doneWhenDeparting = tag.getBoolean("doneWhenDeparting");
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
