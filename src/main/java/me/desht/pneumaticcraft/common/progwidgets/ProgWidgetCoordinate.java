package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinate extends ProgWidget implements IVariableWidget {

    private int x, y, z;
    private String variable = "";
    private boolean useVariable;
    private DroneAIManager aiManager;

    public ProgWidgetCoordinate() {
        super(ModProgWidgets.COORDINATE);
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType returnType() {
        return ModProgWidgets.COORDINATE;
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return ImmutableList.of(ModProgWidgets.COORDINATE);
    }

    @Override
    public void addWarnings(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addWarnings(curInfo, widgets);
        if (!useVariable && x == 0 && y == 0 && z == 0) {
            curInfo.add(xlate("gui.progWidget.coordinate.warning.noCoordinate"));
        }
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (useVariable && variable.equals("")) {
            curInfo.add(xlate("gui.progWidget.general.error.emptyVariable"));
        }
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.GREEN;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_COORDINATE;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putInt("posX", x);
        tag.putInt("posY", y);
        tag.putInt("posZ", z);
        tag.putString("variable", variable);
        tag.putBoolean("useVariable", useVariable);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        x = tag.getInt("posX");
        y = tag.getInt("posY");
        z = tag.getInt("posZ");
        variable = tag.getString("variable");
        useVariable = tag.getBoolean("useVariable");
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    public BlockPos getCoordinate() {
        if (useVariable) {
            return aiManager.getCoordinate(variable);
        } else {
            return getRawCoordinate();
        }
    }

    public BlockPos getRawCoordinate() {
        return new BlockPos(x, y, z);
    }

    public void setCoordinate(BlockPos pos) {
        if (pos != null) {
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();
        } else {
            x = y = z = 0;
        }
    }

    public void setVariable(String varName) {
        variable = varName;
    }

    public String getVariable() {
        return variable;
    }

    public boolean isUsingVariable() {
        return useVariable;
    }

    public void setUsingVariable(boolean useVariable) {
        this.useVariable = useVariable;
    }
    
    public void loadFromGPSTool(ItemStack gpsTool){
        String variable = ItemGPSTool.getVariable(gpsTool);
        if("".equals(variable)){
            setCoordinate(ItemGPSTool.getGPSLocation(gpsTool));
            setUsingVariable(false);
        }else{
            setVariable("#" + variable);
            setUsingVariable(true);
        }
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);

        if (useVariable) {
            curTooltip.add(new StringTextComponent("XYZ: '" + variable + "'"));
        } else if (x != 0 || y != 0 || z != 0) {
            curTooltip.add(new StringTextComponent("X: " + x + ", Y: " + y + ", Z: " + z));
        }
    }

    @Override
    public String getExtraStringInfo() {
        if (useVariable) return "\"" + variable + "\"";
        else return x != 0 || y != 0 || z != 0 ? x + ", " + y + ", " + z : null;
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }
}
