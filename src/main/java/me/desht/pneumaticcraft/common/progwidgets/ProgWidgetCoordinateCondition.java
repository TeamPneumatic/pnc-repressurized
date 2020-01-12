package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.progwidgets.ICondition.Operator;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator.EnumOperator;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinateCondition extends ProgWidgetConditionBase {

    public final boolean[] checkingAxis = new boolean[3];
    private Operator operator = Operator.HIGHER_THAN_EQUALS;

    public ProgWidgetCoordinateCondition() {
        super(ModProgWidgets.CONDITION_COORDINATE.get());
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return ImmutableList.of(ModProgWidgets.COORDINATE.get(), ModProgWidgets.COORDINATE.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (!checkingAxis[0] && !checkingAxis[1] && !checkingAxis[2])
            curInfo.add(xlate("gui.progWidget.conditionCoordinate.error.noAxisSelected"));
    }

    @Override
    public boolean evaluate(IDroneBase drone, IProgWidget widget) {
        BlockPos pos1 = ProgWidgetCoordinateOperator.calculateCoordinate(widget, 0, EnumOperator.PLUS_MINUS);
        BlockPos pos2 = ProgWidgetCoordinateOperator.calculateCoordinate(widget, 1, EnumOperator.PLUS_MINUS);
        if (checkingAxis[0] && !evaluate(pos1.getX(), pos2.getX())) return false;
        if (checkingAxis[1] && !evaluate(pos1.getY(), pos2.getY())) return false;
        return !(checkingAxis[2] && !evaluate(pos1.getZ(), pos2.getZ()));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean evaluate(int arg1, int arg2) {
        return operator.evaluate(arg1, arg2);
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("checkX", checkingAxis[0]);
        tag.putBoolean("checkY", checkingAxis[1]);
        tag.putBoolean("checkZ", checkingAxis[2]);
        tag.putByte("operator", (byte) operator.ordinal());
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        checkingAxis[0] = tag.getBoolean("checkX");
        checkingAxis[1] = tag.getBoolean("checkY");
        checkingAxis[2] = tag.getBoolean("checkZ");
        operator = Operator.values()[tag.getByte("operator")];
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_COORDINATE;
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(new StringTextComponent("Condition: \"" + getCondition() + "\""));
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
