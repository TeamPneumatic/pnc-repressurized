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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.progwidgets.ICondition.Operator;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetCoordinateOperator.EnumOperator;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinateCondition extends ProgWidgetConditionBase {
    public static final MapCodec<ProgWidgetCoordinateCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(builder.group(
                    AxisOptions.CODEC.fieldOf("options").forGetter(ProgWidgetCoordinateCondition::getAxisOptions),
                    StringRepresentable.fromEnum(Operator::values).fieldOf("op").forGetter(ProgWidgetCoordinateCondition::getOperator)
            )
    ).apply(builder, ProgWidgetCoordinateCondition::new));

    private final AxisOptions axisOptions;
    private Operator operator = Operator.GE;

    public ProgWidgetCoordinateCondition() {
        super(PositionFields.DEFAULT);

        axisOptions = new AxisOptions(false, false, false);
        operator = Operator.GE;
    }

    private ProgWidgetCoordinateCondition(PositionFields pos, AxisOptions options, Operator op) {
        super(pos);

        this.axisOptions = options;
        this.operator = op;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.COORDINATE.get(), ModProgWidgetTypes.COORDINATE.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (!axisOptions.shouldCheck(Axis.X) && !axisOptions.shouldCheck(Axis.Y) && !axisOptions.shouldCheck(Axis.Z))
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.conditionCoordinate.error.noAxisSelected"));
    }

    @Override
    public boolean evaluate(IDrone drone, IProgWidget widget) {
        BlockPos pos1 = ProgWidgetCoordinateOperator.calculateCoordinate(widget, 0, EnumOperator.PLUS_MINUS);
        BlockPos pos2 = ProgWidgetCoordinateOperator.calculateCoordinate(widget, 1, EnumOperator.PLUS_MINUS);
        return (!axisOptions.shouldCheck(Axis.X) || evaluate(pos1.getX(), pos2.getX()))
                && (!axisOptions.shouldCheck(Axis.Y) || evaluate(pos1.getY(), pos2.getY()))
                && !(axisOptions.shouldCheck(Axis.Z) && !evaluate(pos1.getZ(), pos2.getZ()));
    }

    public AxisOptions getAxisOptions() {
        return axisOptions;
    }

    private boolean evaluate(int arg1, int arg2) {
        return operator.evaluate(arg1, arg2);
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

//    @Override
//    public void writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.writeToNBT(tag, provider);
//        axisOptions.writeToNBT(tag);
//        tag.putByte("operator", (byte) operator.ordinal());
//    }
//
//    @Override
//    public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.readFromNBT(tag, provider);
//        axisOptions.readFromNBT(tag, false);
//        operator = Operator.values()[tag.getByte("operator")];
//    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        super.writeToPacket(buf);
        axisOptions.writeToBuffer(buf);
        buf.writeByte(operator.ordinal());
    }

    @Override
    public void readFromPacket(RegistryFriendlyByteBuf buf) {
        super.readFromPacket(buf);
        axisOptions.readFromBuffer(buf);
        operator = Operator.values()[buf.readByte()];
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_COORDINATE;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.CONDITION_COORDINATE.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(Component.literal("Condition: \"" + getCondition() + "\""));
    }

    @Override
    public List<Component> getExtraStringInfo() {
        String condition = getCondition();
        return condition.isEmpty() ? Collections.emptyList() : Collections.singletonList(Component.literal(condition));
    }

    public String getCondition() {
        return Arrays.stream(Axis.values())
                .filter(axisOptions::shouldCheck)
                .map(axis -> String.format("%1$s1 %2$s %1$s2", axis.getName(), operator.toString()))
                .collect(Collectors.joining(" and "));
    }
}
