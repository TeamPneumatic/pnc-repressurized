package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIForEachCoordinate;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgWidgetForEachCoordinate extends ProgWidgetAreaItemBase implements IJumpBackWidget, IJump,
        IVariableSetWidget {
    private String elementVariable = "";
    private final Set<BlockPos> traversedPositions = new HashSet<>();
    private DroneAIForEachCoordinate ai;

    public ProgWidgetForEachCoordinate() {
        super(ModProgWidgets.FOR_EACH_COORDINATE);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.YELLOW;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_FOR_EACH_COORDINATE;
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA, ModProgWidgets.TEXT);
    }

    @Override
    public void addVariables(Set<String> variables) {
        super.addVariables(variables);
        variables.add(elementVariable);
    }

    @Override
    public String getVariable() {
        return elementVariable;
    }

    @Override
    public void setVariable(String variable) {
        elementVariable = variable;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        tag.putString("variable", elementVariable);
        super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        elementVariable = tag.getString("variable");
        super.readFromNBT(tag);
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        List<String> locations = getPossibleJumpLocations();
        if (locations.size() > 0 && ai != null
                && (traversedPositions.size() == 1 || !aiManager.getCoordinate(elementVariable).equals(BlockPos.ZERO))) {
            BlockPos pos = ai.getCurCoord();
            if (pos != null) {
                aiManager.setCoordinate(elementVariable, pos);
                return ProgWidgetJump.jumpToLabel(drone, allWidgets, locations.get(0));
            }
        }
        traversedPositions.clear();
        return super.getOutputWidget(drone, allWidgets);
    }

    @Override
    public List<String> getPossibleJumpLocations() {
        IProgWidget widget = getConnectedParameters()[getParameters().size() - 1];
        ProgWidgetText textWidget = widget != null ? (ProgWidgetText) widget : null;
        List<String> locations = new ArrayList<>();
        if (textWidget != null) locations.add(textWidget.string);
        return locations;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return ai = new DroneAIForEachCoordinate(drone, (ProgWidgetForEachCoordinate) widget);
    }

    public boolean isValidPosition(BlockPos pos) {
        return traversedPositions.add(pos);
    }

    @Override
    public boolean canBeRunByComputers(IDroneBase drone, IProgWidget widget) {
        return false;
    }

    @Override
    public String getExtraStringInfo() {
        return "\"" + elementVariable + "\"";
    }

    @Override
    public boolean canSetParameter(int index) {
        return index != 2;//Don't use the blacklist side of the jump parameter.
    }
}
