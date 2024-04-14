/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.goal.Goal;

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
    public void getTooltip(List<Component> curTooltip) {
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
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (isAndFunction) tag.putBoolean("isAndFunction", true);
        tag.putByte("operator", (byte) operator.ordinal());
        tag.putInt("requiredCount", requiredCount);
        if (!measureVar.isEmpty()) tag.putString("measureVar", measureVar);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        isAndFunction = tag.getBoolean("isAndFunction");
        operator = Operator.values()[tag.getByte("operator")];
        requiredCount = tag.getInt("requiredCount");
        measureVar = tag.getString("measureVar");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(isAndFunction);
        buf.writeEnum(operator);
        buf.writeVarInt(requiredCount);
        buf.writeUtf(measureVar, GlobalVariableManager.MAX_VARIABLE_LEN);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        isAndFunction = buf.readBoolean();
        operator = buf.readEnum(Operator.class);
        requiredCount = buf.readVarInt();
        measureVar = buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN);
    }

    @Override
    public List<Component> getExtraStringInfo() {
        MutableComponent anyAll = xlate(isAndFunction() ? "pneumaticcraft.gui.misc.all" : "pneumaticcraft.gui.misc.any")
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
