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

package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinate extends ProgWidget implements IVariableWidget {

    private int x, y, z;
    private String variable = "";
    private boolean useVariable;
    private DroneAIManager aiManager;

    public ProgWidgetCoordinate() {
        super(ModProgWidgets.COORDINATE.get());
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return ModProgWidgets.COORDINATE.get();
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.COORDINATE.get());
    }

    @Override
    public void addWarnings(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addWarnings(curInfo, widgets);
        if (!useVariable && x == 0 && y == 0 && z == 0) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.coordinate.warning.noCoordinate"));
        }
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (useVariable && variable.equals("")) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.emptyVariable"));
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
        if (!variable.isEmpty()) tag.putString("variable", variable);
        if (useVariable) tag.putBoolean("useVariable", true);
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
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeUtf(variable);
        buf.writeBoolean(useVariable);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        variable = buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN);
        useVariable = buf.readBoolean();
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    public BlockPos getCoordinate() {
        if (useVariable && aiManager != null) {
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
    public List<ITextComponent> getExtraStringInfo() {
        if (useVariable) return Collections.singletonList(varAsTextComponent(variable));
        else return x != 0 || y != 0 || z != 0 ? Collections.singletonList(new StringTextComponent(x + ", " + y + ", " + z)) : Collections.emptyList();
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }
}
