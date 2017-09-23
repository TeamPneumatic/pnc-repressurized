package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiWidgetCoordinateCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.progwidgets.ICondition.Operator;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator.EnumOperator;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ProgWidgetCoordinateCondition extends ProgWidgetConditionBase {

    public final boolean[] checkingAxis = new boolean[3];
    private Operator operator = Operator.HIGHER_THAN_EQUALS;

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetCoordinate.class, ProgWidgetCoordinate.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString() {
        return "conditionCoordinate";
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (!checkingAxis[0] && !checkingAxis[1] && !checkingAxis[2])
            curInfo.add("gui.progWidget.conditionCoordinate.error.noAxisSelected");
    }

    @Override
    public boolean evaluate(IDroneBase drone, IProgWidget widget) {
        BlockPos pos1 = ProgWidgetCoordinateOperator.calculateCoordinate(widget, 0, EnumOperator.PLUS_MINUS);
        BlockPos pos2 = ProgWidgetCoordinateOperator.calculateCoordinate(widget, 1, EnumOperator.PLUS_MINUS);
        if (checkingAxis[0] && !evaluate(pos1.getX(), pos2.getX())) return false;
        if (checkingAxis[1] && !evaluate(pos1.getY(), pos2.getY())) return false;
        return !(checkingAxis[2] && !evaluate(pos1.getZ(), pos2.getZ()));
    }

    private boolean evaluate(int arg1, int arg2) {
        return operator == Operator.EQUALS ? arg1 == arg2 : arg1 >= arg2;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("checkX", checkingAxis[0]);
        tag.setBoolean("checkY", checkingAxis[1]);
        tag.setBoolean("checkZ", checkingAxis[2]);
        tag.setByte("operator", (byte) operator.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        checkingAxis[0] = tag.getBoolean("checkX");
        checkingAxis[1] = tag.getBoolean("checkY");
        checkingAxis[2] = tag.getBoolean("checkZ");
        operator = Operator.values()[tag.getByte("operator")];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiWidgetCoordinateCondition(this, guiProgrammer);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_COORDINATE;
    }

    @Override
    public void getTooltip(List<String> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add("Condition: \"" + getCondition() + "\"");
    }

    @Override
    public String getExtraStringInfo() {
        String condition = getCondition();
        return condition.length() > 0 ? condition : null;
    }

    public String getCondition() {
        char[] axis = new char[]{'x', 'y', 'z'};
        StringBuilder condition = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (checkingAxis[i]) {
                if (condition.length() > 0) condition.append(" and ");
                condition.append(("%s1 " + operator + " %s2").replace("%s", "" + axis[i]));
            }
        }
        return condition.toString();
    }
}
