package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetCoordinateOperator;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Set;

public class ProgWidgetCoordinateOperator extends ProgWidget implements IVariableSetWidget {
    public enum EnumOperator {
        PLUS_MINUS("plus_minus"), MULIPLY_DIVIDE("multiply_divide"), MAX_MIN("max_min");

        public ResourceLocation texture;
        private final String name;

        EnumOperator(String name) {
            this.name = name;
            texture = new ResourceLocation(Textures.PROG_WIDGET_LOCATION + "coordinate_operation_" + name + ".png");
        }

        public String getUnlocalizedName() {
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
    public Class<? extends IProgWidget> returnType() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetCoordinate.class};
    }

    @Override
    public String getWidgetString() {
        return "coordinateOperator";
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.GREY;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (variable.equals("")) {
            curInfo.add("gui.progWidget.general.error.emptyVariable");
        }
        if (operator == EnumOperator.MAX_MIN) {
            if (getConnectedParameters()[0] == null && getConnectedParameters()[getParameters().length] == null) {
                curInfo.add("gui.progWidget.coordinateOperator.noParameter");
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
                coordinateWidget = (ProgWidgetCoordinate) widget.getConnectedParameters()[widget.getParameters().length + argIndex];
                while (coordinateWidget != null) {
                    BlockPos pos = coordinateWidget.getCoordinate();
                    if (pos.getX() != 0 && pos.getY() != 0 && pos.getZ() != 0)
                        curPos = new BlockPos(curPos.getX() / pos.getX(), curPos.getY() / pos.getY(), curPos.getZ() / pos.getZ());
                    coordinateWidget = (ProgWidgetCoordinate) coordinateWidget.getConnectedParameters()[0];
                }
                break;
            case PLUS_MINUS:
                curPos = new BlockPos(0, 0, 0);
                coordinateWidget = (ProgWidgetCoordinate) widget.getConnectedParameters()[argIndex];
                while (coordinateWidget != null) {
                    BlockPos pos = coordinateWidget.getCoordinate();
                    curPos = new BlockPos(curPos.getX() + pos.getX(), curPos.getY() + pos.getY(), curPos.getZ() + pos.getZ());
                    coordinateWidget = (ProgWidgetCoordinate) coordinateWidget.getConnectedParameters()[0];
                }
                coordinateWidget = (ProgWidgetCoordinate) widget.getConnectedParameters()[widget.getParameters().length + argIndex];
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
                coordinateWidget = (ProgWidgetCoordinate) widget.getConnectedParameters()[widget.getParameters().length + argIndex];
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
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("variable", variable);
        tag.setByte("operator", (byte) operator.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        variable = tag.getString("variable");
        byte operatorValue = tag.hasKey("multiplyDivide") ? tag.getByte("multiplyDivide") : tag.getByte("operator");
        operator = EnumOperator.values()[operatorValue];
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
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiProgWidgetCoordinateOperator(this, guiProgrammer);
    }

    @Override
    public void getTooltip(List<String> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add("Setting variable: \"" + variable + "\"");
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
