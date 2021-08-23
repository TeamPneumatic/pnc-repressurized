package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.progwidgets.ICondition.Operator;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator.EnumOperator;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinateCondition extends ProgWidgetConditionBase {
    private final AxisOptions axisOptions = new AxisOptions(false, false, false);
    private Operator operator = Operator.GE;

    public ProgWidgetCoordinateCondition() {
        super(ModProgWidgets.CONDITION_COORDINATE.get());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.COORDINATE.get(), ModProgWidgets.COORDINATE.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (!axisOptions.shouldCheck(Axis.X) && !axisOptions.shouldCheck(Axis.Y) && !axisOptions.shouldCheck(Axis.Z))
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.conditionCoordinate.error.noAxisSelected"));
    }

    @Override
    public boolean evaluate(IDroneBase drone, IProgWidget widget) {
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

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        axisOptions.writeToNBT(tag);
        tag.putByte("operator", (byte) operator.ordinal());
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        axisOptions.readFromNBT(tag, false);
        operator = Operator.values()[tag.getByte("operator")];
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        axisOptions.writeToBuffer(buf);
        buf.writeByte(operator.ordinal());
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        axisOptions.readFromBuffer(buf);
        operator = Operator.values()[buf.readByte()];
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
    public List<ITextComponent> getExtraStringInfo() {
        String condition = getCondition();
        return condition.isEmpty() ? Collections.emptyList() : Collections.singletonList(new StringTextComponent(condition));
    }

    public String getCondition() {
        return Arrays.stream(Axis.values())
                .filter(axisOptions::shouldCheck)
                .map(axis -> String.format("%1$s1 %2$s %1$s2", axis.getName(), operator.toString()))
                .collect(Collectors.joining(" and "));
    }
}
