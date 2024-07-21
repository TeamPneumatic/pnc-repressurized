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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinateOperator extends ProgWidget implements IVariableSetWidget {
    public static final MapCodec<ProgWidgetCoordinateOperator> CODEC = RecordCodecBuilder.mapCodec(builder ->
        baseParts(builder).and(builder.group(
                Codec.STRING.optionalFieldOf("var", "").forGetter(ProgWidgetCoordinateOperator::getVariable),
                StringRepresentable.fromEnum(EnumOperator::values).optionalFieldOf("coord_op", EnumOperator.PLUS_MINUS).forGetter(ProgWidgetCoordinateOperator::getOperator),
                AxisOptions.CODEC.optionalFieldOf("axis_options", AxisOptions.TRUE).forGetter(ProgWidgetCoordinateOperator::getAxisOptions)
        )
    ).apply(builder, ProgWidgetCoordinateOperator::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetCoordinateOperator> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.STRING_UTF8, ProgWidgetCoordinateOperator::getVariable,
            NeoForgeStreamCodecs.enumCodec(EnumOperator.class), ProgWidgetCoordinateOperator::getOperator,
            AxisOptions.STREAM_CODEC, ProgWidgetCoordinateOperator::getAxisOptions,
            ProgWidgetCoordinateOperator::new
    );

    private EnumOperator operator;
    private String variable;
    private DroneAIManager aiManager;
    private final AxisOptions axisOptions;

    public ProgWidgetCoordinateOperator() {
        this(PositionFields.DEFAULT, "", EnumOperator.PLUS_MINUS, AxisOptions.TRUE);
    }

    public ProgWidgetCoordinateOperator(PositionFields positionFields, String variable, EnumOperator operator, AxisOptions axisOptions) {
        super(positionFields);
        this.operator = operator;
        this.variable = variable;
        this.axisOptions = axisOptions;
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetCoordinateOperator(getPosition(), variable, operator, axisOptions.copy());
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.COORDINATE.get());
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
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (variable.isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.emptyVariable"));
        }
        if (!axisOptions.shouldCheck(Axis.X) && !axisOptions.shouldCheck(Axis.Y) && !axisOptions.shouldCheck(Axis.Z)) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.conditionCoordinate.error.noAxisSelected"));
        }
        if (operator == EnumOperator.MAX_MIN) {
            if (getConnectedParameters()[0] == null && getConnectedParameters()[getParameters().size()] == null) {
                curInfo.add(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.noParameter"));
            }
        } else if (operator == EnumOperator.MULIPLY_DIVIDE) {
            IProgWidget w = getConnectedParameters()[1];
            while (w instanceof ProgWidgetCoordinate pwc) {
                if (!pwc.isUsingVariable()) {
                    BlockPos pos = pwc.getCoordinate().orElse(BlockPos.ZERO);
                    if (axisOptions.shouldCheck(Axis.X) && pos.getX() == 0
                            || axisOptions.shouldCheck(Axis.Y) && pos.getY() == 0
                            || axisOptions.shouldCheck(Axis.Z) && pos.getZ() == 0)
                    {
                        curInfo.add(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.divideByZero"));
                        break;
                    }
                }
                w = w.getConnectedParameters()[0];
            }
        }
    }

    @Override
    public IProgWidget getOutputWidget(IDrone drone, List<IProgWidget> allWidgets) {
        if (!variable.isEmpty()) {
            BlockPos curPos = calculateCoordinate(this, 0, operator, axisOptions);
            aiManager.setCoordinate(variable, curPos);
        }
        return super.getOutputWidget(drone, allWidgets);
    }

    public static BlockPos calculateCoordinate(IProgWidget widget, int argIndex, EnumOperator operator, AxisOptions axisOptions) {
        return doCalc(
                (ProgWidgetCoordinate) widget.getConnectedParameters()[argIndex],
                (ProgWidgetCoordinate) widget.getConnectedParameters()[argIndex + widget.getParameters().size()],
                operator, axisOptions
        );
    }

    public static BlockPos calculateCoordinate(IProgWidget widget, int argIndex, EnumOperator operator) {
        return calculateCoordinate(widget, argIndex, operator, AxisOptions.TRUE);
    }

    private static BlockPos doCalc(ProgWidgetCoordinate whiteList, ProgWidgetCoordinate blackList, EnumOperator op, AxisOptions axisOptions) {
        BlockPos curPos = op.initialValue(whiteList, blackList);
        if (whiteList != null) {
            whiteList = (ProgWidgetCoordinate) whiteList.getConnectedParameters()[0];
            while (whiteList != null) {
                curPos = getNextPos(curPos, whiteList.getCoordinate().orElse(BlockPos.ZERO), op, true, axisOptions);
                whiteList = (ProgWidgetCoordinate) whiteList.getConnectedParameters()[0];
            }
        } else if (blackList != null) {
            // we already picked up the first blacklist coord via op.initialValue()
            blackList = (ProgWidgetCoordinate) blackList.getConnectedParameters()[0];
        }
        while (blackList != null) {
            curPos = getNextPos(curPos, blackList.getCoordinate().orElse(BlockPos.ZERO), op, false, axisOptions);
            blackList = (ProgWidgetCoordinate) blackList.getConnectedParameters()[0];
        }
        return curPos;
    }

    private static BlockPos getNextPos(BlockPos curPos, BlockPos rawPos, EnumOperator op, boolean isWhiteList, AxisOptions axisOptions) {
        BlockPos pos = new BlockPos(
                axisOptions.shouldCheck(Axis.X) ? rawPos.getX() : op.defaultValue(isWhiteList),
                axisOptions.shouldCheck(Axis.Y) ? rawPos.getY() : op.defaultValue(isWhiteList),
                axisOptions.shouldCheck(Axis.Z) ? rawPos.getZ() : op.defaultValue(isWhiteList)
        );
        return op.apply(curPos, pos, isWhiteList);
    }

    @Override
    public ResourceLocation getTexture() {
        return operator.texture;
    }

    public EnumOperator getOperator() {
        return operator;
    }

    public void setOperator(EnumOperator operator) {
        this.operator = operator;
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
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    public AxisOptions getAxisOptions() {
        return axisOptions;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.COORDINATE_OPERATOR.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);

        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.itemAssign.settingVariable", variable));
        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.operator").append(" ").append(xlate(operator.getTranslationKey())));
        getAxesString().ifPresent(t -> curTooltip.add(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.axes").append(" ").append(t)));
    }

    @Override
    public List<Component> getExtraStringInfo() {
        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.add(varAsTextComponent(variable), xlate(operator.getTranslationKey()));
        getAxesString().ifPresent(builder::add);
        return builder.build();
    }

    private Optional<Component> getAxesString() {
        List<String> l = Arrays.stream(Axis.values())
                .filter(axisOptions::shouldCheck)
                .map(axis -> axis.getName().toUpperCase())
                .collect(Collectors.toList());
        return !l.isEmpty() && l.size() < 3 ? Optional.of(Component.literal(String.join("/", l))) : Optional.empty();
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }

    public enum EnumOperator implements ITranslatableEnum, StringRepresentable {
        PLUS_MINUS("plus_minus", 0, 0),
        MULIPLY_DIVIDE("multiply_divide", 1, 1),
        MAX_MIN("max_min", Integer.MIN_VALUE, Integer.MAX_VALUE);

        public final ResourceLocation texture;
        private final String name;
        private final int defWhite;
        private final int defBlack;

        EnumOperator(String name, int defWhite, int defBlack) {
            this.name = name;
            this.texture = Textures.progWidgetTexture("coordinate_operation_" + name + ".png");
            this.defWhite = defWhite;
            this.defBlack = defBlack;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.coordinateOperator." + name;
        }

        public int defaultValue(boolean isWhiteList) {
            // a default to return if the relevant x/y/z field is not being operated on
            return isWhiteList ? defWhite : defBlack;
        }

        public BlockPos initialValue(ProgWidgetCoordinate whiteList, ProgWidgetCoordinate blackList) {
            return switch (this) {
                case PLUS_MINUS -> whiteList != null ?
                        whiteList.getCoordinate().orElse(BlockPos.ZERO) :
                        (blackList != null ? BlockPos.ZERO.subtract(blackList.getCoordinate().orElse(BlockPos.ZERO)) : BlockPos.ZERO);
                case MULIPLY_DIVIDE -> whiteList != null ? whiteList.getCoordinate().orElse(BlockPos.ZERO) : BlockPos.ZERO;
                case MAX_MIN -> whiteList != null ?
                        whiteList.getCoordinate().orElse(BlockPos.ZERO) :
                        (blackList != null ? blackList.getCoordinate().orElse(BlockPos.ZERO) : BlockPos.ZERO);
            };
        }

        public BlockPos apply(BlockPos p1, BlockPos p2, boolean isWhiteList) {
            return switch (this) {
                case PLUS_MINUS -> isWhiteList ?
                        new BlockPos(p1.getX() + p2.getX(), p1.getY() + p2.getY(), p1.getZ() + p2.getZ()) :
                        new BlockPos(p1.getX() - p2.getX(), p1.getY() - p2.getY(), p1.getZ() - p2.getZ());
                case MULIPLY_DIVIDE -> {
                    if (!isWhiteList && (p2.getX() == 0 || p2.getY() == 0 || p2.getZ() == 0)) yield p1;
                    yield isWhiteList ?
                            new BlockPos(p1.getX() * p2.getX(), p1.getY() * p2.getY(), p1.getZ() * p2.getZ()) :
                            new BlockPos(p1.getX() / p2.getX(), p1.getY() / p2.getY(), p1.getZ() / p2.getZ());  // no divide by zero!
                }
                case MAX_MIN -> isWhiteList ?
                        new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ())) :
                        new BlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
