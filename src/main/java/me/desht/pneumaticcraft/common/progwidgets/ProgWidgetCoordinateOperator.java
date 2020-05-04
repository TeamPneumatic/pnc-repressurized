package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinateOperator extends ProgWidget implements IVariableSetWidget {
    public ProgWidgetCoordinateOperator() {
        super(ModProgWidgets.COORDINATE_OPERATOR);
    }

    public enum EnumOperator {
        PLUS_MINUS("plus_minus"), MULIPLY_DIVIDE("multiply_divide"), MAX_MIN("max_min");

        public final ResourceLocation texture;
        private final String name;

        EnumOperator(String name) {
            this.name = name;
            this.texture = Textures.progWidgetTexture("coordinate_operation_" + name + ".png");
        }

        public String getTranslationKey() {
            return "gui.progWidget.coordinateOperator." + name;
        }
    }

    private EnumOperator operator = EnumOperator.PLUS_MINUS;
    private String variable = "";
    private DroneAIManager aiManager;

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.COORDINATE);
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
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (variable.equals("")) {
            curInfo.add(xlate("gui.progWidget.general.error.emptyVariable"));
        }
        if (operator == EnumOperator.MAX_MIN) {
            if (getConnectedParameters()[0] == null && getConnectedParameters()[getParameters().size()] == null) {
                curInfo.add(xlate("gui.progWidget.coordinateOperator.noParameter"));
            }
        } else if (operator == EnumOperator.MULIPLY_DIVIDE) {
            IProgWidget w = getConnectedParameters()[1];
            while (w instanceof ProgWidgetCoordinate) {
                BlockPos pos = ((ProgWidgetCoordinate) w).getCoordinate();
                if (pos.getX() == 0 || pos.getY() == 0 || pos.getZ() == 0) {
                    curInfo.add(xlate("gui.progWidget.coordinateOperator.divideByZero"));
                    break;
                }
                w = w.getConnectedParameters()[0];
            }
        }
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        if (!variable.equals("")) {
            BlockPos curPos = calculateCoordinate(this, 0, operator);
            aiManager.setCoordinate(variable, curPos);
        }
        return super.getOutputWidget(drone, allWidgets);
    }

    public static BlockPos calculateCoordinate(IProgWidget widget, int argIndex, EnumOperator operator) {
        BlockPos curPos = null;
        switch (operator) {
            case MULIPLY_DIVIDE:
                curPos = new BlockPos(1, 1, 1);
                ProgWidgetCoordinate coordinateWidget = (ProgWidgetCoordinate) widget.getConnectedParameters()[argIndex];
                while (coordinateWidget != null) {
                    BlockPos pos = coordinateWidget.getCoordinate();
                    curPos = new BlockPos(curPos.getX() * pos.getX(), curPos.getY() * pos.getY(), curPos.getZ() * pos.getZ());
                    coordinateWidget = (ProgWidgetCoordinate) coordinateWidget.getConnectedParameters()[0];
                }
                coordinateWidget = (ProgWidgetCoordinate) widget.getConnectedParameters()[widget.getParameters().size()+ argIndex];
                while (coordinateWidget != null) {
                    BlockPos pos = coordinateWidget.getCoordinate();
                    if (pos.getX() != 0 && pos.getY() != 0 && pos.getZ() != 0)
                        curPos = new BlockPos(curPos.getX() / pos.getX(), curPos.getY() / pos.getY(), curPos.getZ() / pos.getZ());
                    coordinateWidget = (ProgWidgetCoordinate) coordinateWidget.getConnectedParameters()[0];
                }
                break;
            case PLUS_MINUS:
                curPos = BlockPos.ZERO;
                coordinateWidget = (ProgWidgetCoordinate) widget.getConnectedParameters()[argIndex];
                while (coordinateWidget != null) {
                    BlockPos pos = coordinateWidget.getCoordinate();
                    curPos = new BlockPos(curPos.getX() + pos.getX(), curPos.getY() + pos.getY(), curPos.getZ() + pos.getZ());
                    coordinateWidget = (ProgWidgetCoordinate) coordinateWidget.getConnectedParameters()[0];
                }
                coordinateWidget = (ProgWidgetCoordinate) widget.getConnectedParameters()[widget.getParameters().size() + argIndex];
                while (coordinateWidget != null) {
                    BlockPos pos = coordinateWidget.getCoordinate();
                    curPos = new BlockPos(curPos.getX() - pos.getX(), curPos.getY() - pos.getY(), curPos.getZ() - pos.getZ());
                    coordinateWidget = (ProgWidgetCoordinate) coordinateWidget.getConnectedParameters()[0];
                }
                break;
            case MAX_MIN:
                curPos = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
                coordinateWidget = (ProgWidgetCoordinate) widget.getConnectedParameters()[argIndex];
                while (coordinateWidget != null) {
                    BlockPos pos = coordinateWidget.getCoordinate();
                    curPos = new BlockPos(Math.max(curPos.getX(), pos.getX()), Math.max(curPos.getY(), pos.getY()), Math.max(curPos.getZ(), pos.getZ()));
                    coordinateWidget = (ProgWidgetCoordinate) coordinateWidget.getConnectedParameters()[0];
                }
                if (curPos.equals(new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE)))
                    curPos = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
                coordinateWidget = (ProgWidgetCoordinate) widget.getConnectedParameters()[widget.getParameters().size() + argIndex];
                while (coordinateWidget != null) {
                    BlockPos pos = coordinateWidget.getCoordinate();
                    curPos = new BlockPos(Math.min(curPos.getX(), pos.getX()), Math.min(curPos.getY(), pos.getY()), Math.min(curPos.getZ(), pos.getZ()));
                    coordinateWidget = (ProgWidgetCoordinate) coordinateWidget.getConnectedParameters()[0];
                }
                break;
        }
        return curPos;
    }

    @Override
    public ResourceLocation getTexture() {
        return operator.texture;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putString("variable", variable);
        tag.putByte("operator", (byte) operator.ordinal());
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        variable = tag.getString("variable");
        byte operatorValue = tag.contains("multiplyDivide") ? tag.getByte("multiplyDivide") : tag.getByte("operator");
        operator = EnumOperator.values()[operatorValue];
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeString(variable);
        buf.writeByte(operator.ordinal());
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        variable = buf.readString(GlobalVariableManager.MAX_VARIABLE_LEN);
        operator = EnumOperator.values()[buf.readByte()];
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

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(new StringTextComponent("Setting variable: \"" + variable + "\""));
    }

    @Override
    public String getExtraStringInfo() {
        return "\"" + variable + "\"";
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }
}
