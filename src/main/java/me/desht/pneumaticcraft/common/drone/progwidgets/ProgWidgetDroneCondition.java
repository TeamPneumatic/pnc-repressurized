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
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ProgWidgetDroneCondition extends ProgWidgetConditionBase implements ICondition, IVariableSetWidget {
    protected static <P extends ProgWidgetDroneCondition> Products.P2<RecordCodecBuilder.Mu<P>, PositionFields, DroneConditionFields> droneConditionParts(RecordCodecBuilder.Instance<P> pInstance) {
        return baseParts(pInstance).and(DroneConditionFields.CODEC.fieldOf("drone_cond").forGetter(p -> p.cond));
    }

    protected DroneConditionFields cond;

    public ProgWidgetDroneCondition() {
        this(PositionFields.DEFAULT, DroneConditionFields.DEFAULT);
    }

    public ProgWidgetDroneCondition(PositionFields pos, DroneConditionFields cond) {
        super(pos);

        this.cond = cond;
    }

    @Override
    public boolean evaluate(IDrone drone, IProgWidget widget) {
        return getOperator().evaluate(getCount(drone, widget), getRequiredCount());
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        if (!getMeasureVar().isEmpty()) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.condition.measure").append(getMeasureVar()));
        }
    }

    protected abstract int getCount(IDrone drone, IProgWidget widget);

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
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
    public boolean isAndFunction() {
        return cond.isAndFunc;
    }

    @Override
    public void setAndFunction(boolean isAndFunction) {
        this.cond = cond.withIsAndFunc(isAndFunction);
    }

    @Override
    public int getRequiredCount() {
        return cond.requiredCount;
    }

    @Override
    public void setRequiredCount(int count) {
        this.cond = cond.withRequiredCount(count);
    }

    @Override
    public Operator getOperator() {
        return cond.op;
    }

    @Override
    public void setOperator(Operator operator) {
        this.cond = cond.withOp(operator);
    }
    @Override
    public String getMeasureVar() {
        return cond.measureVar;
    }

    @Override
    public void setMeasureVar(String measureVar) {
        this.cond = cond.withMeasureVar(measureVar);
    }

    @Override
    public List<Component> getExtraStringInfo() {
        MutableComponent anyAll = xlate(isAndFunction() ? "pneumaticcraft.gui.misc.all" : "pneumaticcraft.gui.misc.any")
                .append(" " + getOperator().toString() + " " + getRequiredCount());
        return getMeasureVar().isEmpty() ? Collections.singletonList(anyAll) : ImmutableList.of(anyAll, varAsTextComponent(getMeasureVar()));
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

    public DroneConditionFields droneConditionFields() {
        return cond;
    }

    public record DroneConditionFields(boolean isAndFunc, Operator op, int requiredCount, String measureVar) {
        public static final DroneConditionFields DEFAULT = new DroneConditionFields(false, Operator.GE, 1, "");

        public static final Codec<DroneConditionFields> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.BOOL.optionalFieldOf("and_func", false).forGetter(DroneConditionFields::isAndFunc),
                StringRepresentable.fromEnum(Operator::values).optionalFieldOf("cond_op", Operator.GE).forGetter(DroneConditionFields::op),
                Codec.INT.optionalFieldOf("required_count", 1).forGetter(DroneConditionFields::requiredCount),
                Codec.STRING.optionalFieldOf("measure_var", "").forGetter(DroneConditionFields::measureVar)
        ).apply(builder, DroneConditionFields::new));
        public static final StreamCodec<FriendlyByteBuf, DroneConditionFields> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, DroneConditionFields::isAndFunc,
                NeoForgeStreamCodecs.enumCodec(Operator.class), DroneConditionFields::op,
                ByteBufCodecs.VAR_INT, DroneConditionFields::requiredCount,
                ByteBufCodecs.STRING_UTF8, DroneConditionFields::measureVar,
                DroneConditionFields::new
        );

        public DroneConditionFields withIsAndFunc(boolean isAndFunc) {
            return new DroneConditionFields(isAndFunc, op, requiredCount, measureVar);
        }

        public DroneConditionFields withOp(Operator op) {
            return new DroneConditionFields(isAndFunc, op, requiredCount, measureVar);
        }

        public DroneConditionFields withRequiredCount(int requiredCount) {
            return new DroneConditionFields(isAndFunc, op, requiredCount, measureVar);
        }

        public DroneConditionFields withMeasureVar(String measureVar) {
            return new DroneConditionFields(isAndFunc, op, requiredCount, measureVar);
        }
    }
}
