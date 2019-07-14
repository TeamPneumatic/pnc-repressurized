package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetItemAssign extends ProgWidget implements IVariableSetWidget {
    private String variable = "";
    private DroneAIManager aiManager;

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public boolean hasBlacklist() {
        return false;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetItemFilter.class};
    }

    @Override
    public String getWidgetString() {
        return "itemAssign";
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.GRAY;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (variable.equals("")) {
            curInfo.add(xlate("gui.progWidget.general.error.emptyVariable"));
        }
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_ITEM_ASSIGN;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        if (!variable.equals("")) {
            ProgWidgetItemFilter filter = (ProgWidgetItemFilter) getConnectedParameters()[0];
            aiManager.setItem(variable, filter != null ? filter.getFilter() : null);
        }
        return super.getOutputWidget(drone, allWidgets);
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putString("variable", variable);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        variable = tag.getString("variable");
    }

    @Override
    public String getVariable() {
        return variable;
    }

    @Override
    public void setVariable(String variable) {
        this.variable = variable;
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(new StringTextComponent("Setting variable: \"" + variable + "\""));
    }

    @Override
    public String getExtraStringInfo() {
        return "\"" + variable + "\"";
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }
}
