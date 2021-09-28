package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ProgWidgetDroneCondition extends ProgWidgetConditionBase implements ICondition, IVariableSetWidget {
    private boolean isAndFunction;
    private ICondition.Operator operator = ICondition.Operator.GE;
    private int requiredCount = 1;
    private String measureVar = "";

    public ProgWidgetDroneCondition(ProgWidgetType<?> type) {
        super(type);
    }

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

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        if (!measureVar.isEmpty()) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.condition.measure").append(measureVar));
        }
    }

    protected abstract int getCount(IDroneBase drone, IProgWidget widget);

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        if (widget instanceof ProgWidgetDroneCondition) {
            return null;
        } else {
            return new Goal() {//Trick the CC program into thinking this is an executable piece.
                @Override
                public boolean canUse() {
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
    public String getMeasureVar() {
        return measureVar;
    }

    @Override
    public void setMeasureVar(String measureVar) {
        this.measureVar = measureVar;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        if (isAndFunction) tag.putBoolean("isAndFunction", true);
        tag.putByte("operator", (byte) operator.ordinal());
        tag.putInt("requiredCount", requiredCount);
        if (!measureVar.isEmpty()) tag.putString("measureVar", measureVar);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        isAndFunction = tag.getBoolean("isAndFunction");
        operator = Operator.values()[tag.getByte("operator")];
        requiredCount = tag.getInt("requiredCount");
        measureVar = tag.getString("measureVar");
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(isAndFunction);
        buf.writeByte(operator.ordinal());
        buf.writeVarInt(requiredCount);
        buf.writeUtf(measureVar, GlobalVariableManager.MAX_VARIABLE_LEN);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        isAndFunction = buf.readBoolean();
        operator = Operator.values()[buf.readByte()];
        requiredCount = buf.readVarInt();
        measureVar = buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN);
    }

    @Override
    public List<ITextComponent> getExtraStringInfo() {
        IFormattableTextComponent anyAll = xlate(isAndFunction() ? "pneumaticcraft.gui.misc.all" : "pneumaticcraft.gui.misc.any")
                .append(" " + getOperator().toString() + " " + getRequiredCount());
        return measureVar.isEmpty() ? Collections.singletonList(anyAll) : ImmutableList.of(anyAll, varAsTextComponent(measureVar));
    }

    @Override
    public void addVariables(Set<String> variables) {
        if (!getMeasureVar().isEmpty()) {
            variables.add(getMeasureVar());
        }
    }

    @Override
    public String getVariable() {
        return getMeasureVar();
    }

    @Override
    public void setVariable(String variable) {
        setMeasureVar(variable);
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        // no-op
    }
}
