package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.CompoundNBT;

public abstract class ProgWidgetDroneCondition extends ProgWidgetConditionBase implements ICondition {

    private boolean isAndFunction;
    private ICondition.Operator operator = ICondition.Operator.HIGHER_THAN_EQUALS;
    private int requiredCount = 1;

    @Override
    public boolean isAndFunction() {
        return isAndFunction;
    }

    @Override
    public void setAndFunction(boolean isAndFunction) {
        this.isAndFunction = isAndFunction;
    }

    @Override
    public boolean evaluate(IDroneBase drone, IProgWidget widget) {
        return getOperator().evaluate(getCount(drone, widget), getRequiredCount());
    }

    protected abstract int getCount(IDroneBase drone, IProgWidget widget);

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        if (widget instanceof ProgWidgetDroneCondition) {
            return null;
        } else {
            return new Goal() {//Trick the CC program into thinking this is an executable piece.
                @Override
                public boolean shouldExecute() {
                    return false;
                }
            };
        }
    }

    @Override
    public int getRequiredCount() {
        return requiredCount;
    }

    @Override
    public void setRequiredCount(int count) {
        requiredCount = count;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("isAndFunction", isAndFunction);
        tag.putByte("operator", (byte) operator.ordinal());
        tag.putInt("requiredCount", requiredCount);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        isAndFunction = tag.getBoolean("isAndFunction");
        operator = ICondition.Operator.values()[tag.getByte("operator")];
        requiredCount = tag.getInt("requiredCount");
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public Screen getOptionWindow(GuiProgrammer guiProgrammer) {
//        return new GuiProgWidgetCondition(this, guiProgrammer) {
//            @Override
//            protected boolean isUsingAndOr() {
//                return false;
//            }
//        };
//    }

    @Override
    public String getExtraStringInfo() {
        String anyAll = I18n.format(isAndFunction() ? "gui.progWidget.condition.all" : "gui.progWidget.condition.any");
        return anyAll + " " + getOperator().toString() + " " + getRequiredCount();
    }

}
