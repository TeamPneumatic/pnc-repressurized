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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ProgWidgetCC;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Base class for in-world conditions.
 */
public abstract class ProgWidgetCondition extends ProgWidgetInventoryBase implements ICondition, IJump, IVariableSetWidget {
    protected static <P extends ProgWidgetCondition> Products.P3<RecordCodecBuilder.Mu<P>, PositionFields, InvBaseFields, ConditionFields> condParts(RecordCodecBuilder.Instance<P> pInstance) {
        return invParts(pInstance).and(ConditionFields.CODEC.fieldOf("cond").forGetter(p -> p.cond));
    }

    private DroneAIBlockCondition evaluator;
    protected ConditionFields cond;

    protected ProgWidgetCondition(PositionFields pos, InvBaseFields inv, ConditionFields cond) {
        super(pos, inv);

        this.cond = cond;
    }

    protected ProgWidgetCondition() {
        this(PositionFields.DEFAULT, InvBaseFields.DEFAULT, ConditionFields.DEFAULT);
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        // when running under computer control, it appears to be important to keep the same evaluator
        //  and the reverse is true when not running under computer control (i.e. normal drone program)
        // TODO needs more investigation
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/984
        if (evaluator == null || !(widget instanceof ProgWidgetCC)) evaluator = getEvaluator(drone, widget);

        return evaluator;
    }

    protected abstract DroneAIBlockCondition getEvaluator(IDrone drone, IProgWidget widget);

    @Override
    public String getMeasureVar() {
        return Objects.requireNonNullElse(cond.measureVar, "");
    }

    @Override
    public void setMeasureVar(String measureVar) {
        this.cond = cond.withMeasureVar(measureVar);
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        if (!getMeasureVar().isEmpty()) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.condition.measure").append(getMeasureVar()));
        }
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getMeasureVar().isEmpty() && getConnectedParameters()[getParameters().size() - 1] == null && getConnectedParameters()[getParameters().size() * 2 - 1] == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.condition.error.noFlowControl"));
        }
    }

    @Override
    public IProgWidget getOutputWidget(IDrone drone, List<IProgWidget> allWidgets) {
        if (evaluator != null) {
            boolean evaluation = evaluate(drone, this);
            if (evaluation) {
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.condition.evaluatedTrue");
            } else {
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.condition.evaluatedFalse");
            }
            return ProgWidgetJump.jumpToLabel(drone, allWidgets, this, evaluation);
        } else {
            Log.error("Shouldn't be happening! ProgWidgetCondition evaluator is null for some reason");
            return super.getOutputWidget(drone, allWidgets);
        }
    }

    @Override
    public boolean evaluate(IDrone drone, IProgWidget widget) {
        return evaluator.getResult();
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
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.MEDIUM;
    }

    @Override
    public List<String> getPossibleJumpLocations() {
        IProgWidget widget = getConnectedParameters()[getParameters().size() - 1];
        ProgWidgetText textWidget = widget != null ? (ProgWidgetText) widget : null;
        IProgWidget widget2 = getConnectedParameters()[getParameters().size() * 2 - 1];
        ProgWidgetText textWidget2 = widget2 != null ? (ProgWidgetText) widget2 : null;
        List<String> locations = new ArrayList<>();
        if (textWidget != null) locations.add(textWidget.string);
        if (textWidget2 != null) locations.add(textWidget2.string);
        return locations;
    }

    @Override
    public int getRequiredCount() {
        return getCount();
    }

    @Override
    public void setRequiredCount(int count) {
        setCount(count);
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
    protected boolean isUsingSides() {
        return false;
    }

    @Override
    public List<Component> getExtraStringInfo() {
        MutableComponent anyAll = xlate(isAndFunction() ? "pneumaticcraft.gui.misc.all" : "pneumaticcraft.gui.misc.any")
                .append(" " + getOperator().toString() + " " + getRequiredCount());
        return getMeasureVar().isEmpty() ? Collections.singletonList(anyAll) : ImmutableList.of(anyAll, varAsTextComponent(getMeasureVar()));
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.CYAN;
    }

    @Override
    public void addVariables(Set<String> variables) {
        super.addVariables(variables);

        if (!getMeasureVar().isEmpty()) variables.add(getMeasureVar());
    }

    @Override
    public String getVariable() {
        return getMeasureVar();
    }

    @Override
    public void setVariable(String variable) {
        setMeasureVar(variable);
    }

    public ConditionFields conditionFields() {
        return cond;
    }

    @Override
    protected boolean baseEquals(ProgWidget other) {
        return super.baseEquals(other) && other instanceof ProgWidgetCondition c && cond.equals(c.cond);
    }

    @Override
    protected int baseHashCode() {
        return Objects.hash(positionFields, invBaseFields, cond);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgWidgetCondition that = (ProgWidgetCondition) o;
        return baseEquals(that);
    }

    @Override
    public int hashCode() {
        return baseHashCode();
    }

    public record ConditionFields(boolean isAndFunc, Operator op, String measureVar) {
        public static final ConditionFields DEFAULT = new ConditionFields(false, Operator.GE, "");

        public static final Codec<ConditionFields> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.BOOL.optionalFieldOf("and_func", false).forGetter(ConditionFields::isAndFunc),
                StringRepresentable.fromEnum(Operator::values).optionalFieldOf("cond_op", Operator.GE).forGetter(ConditionFields::op),
                Codec.STRING.optionalFieldOf("measure_var", "").forGetter(ConditionFields::measureVar)
        ).apply(builder, ConditionFields::new));
        public static final StreamCodec<FriendlyByteBuf, ConditionFields> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, ConditionFields::isAndFunc,
                NeoForgeStreamCodecs.enumCodec(Operator.class), ConditionFields::op,
                ByteBufCodecs.STRING_UTF8, ConditionFields::measureVar,
                ConditionFields::new
        );

        public ConditionFields withIsAndFunc(boolean isAndFunc) {
            return new ConditionFields(isAndFunc, op, measureVar);
        }

        public ConditionFields withOp(Operator op) {
            return new ConditionFields(isAndFunc, op, measureVar);
        }

        public ConditionFields withMeasureVar(String measureVar) {
            return new ConditionFields(isAndFunc, op, measureVar);
        }
    }
}
