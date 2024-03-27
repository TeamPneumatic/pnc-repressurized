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
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetItemAssign extends ProgWidget implements IVariableSetWidget {
    private String variable = "";
    private DroneAIManager aiManager;

    public ProgWidgetItemAssign() {
        super(ModProgWidgets.ITEM_ASSIGN.get());
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public boolean hasBlacklist() {
        return false;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.ITEM_FILTER.get());
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
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_ITEM_ASSIGN;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        if (!variable.isEmpty()) {
            ProgWidgetItemFilter filter = (ProgWidgetItemFilter) getConnectedParameters()[0];
            aiManager.setStack(variable, filter != null ? filter.getFilter() : drone.getInv().getStackInSlot(0).copy());
        }
        return super.getOutputWidget(drone, allWidgets);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString("variable", variable);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        variable = tag.getString("variable");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeUtf(variable);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        variable = buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN);
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
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.itemAssign.settingVariable", variable));
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return Collections.singletonList(varAsTextComponent(variable));
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }
}
