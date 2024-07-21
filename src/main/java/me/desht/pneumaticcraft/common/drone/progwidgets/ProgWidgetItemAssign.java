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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetItemAssign extends ProgWidget implements IVariableSetWidget {
    public static final MapCodec<ProgWidgetItemAssign> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(Codec.STRING.optionalFieldOf("var", "").forGetter(ProgWidgetItemAssign::getVariable)
    ).apply(builder, ProgWidgetItemAssign::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetItemAssign> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.STRING_UTF8, ProgWidgetItemAssign::getVariable,
            ProgWidgetItemAssign::new
    );

    private String variable;
    private DroneAIManager aiManager;

    private ProgWidgetItemAssign(PositionFields pos, String variable) {
        super(pos);
        this.variable = variable;
    }

    public ProgWidgetItemAssign() {
        this(PositionFields.DEFAULT, "");
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetItemAssign(getPosition(), variable);
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
        return ImmutableList.of(ModProgWidgetTypes.ITEM_FILTER.get());
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
    public IProgWidget getOutputWidget(IDrone drone, List<IProgWidget> allWidgets) {
        if (!variable.isEmpty()) {
            ProgWidgetItemFilter filter = (ProgWidgetItemFilter) getConnectedParameters()[0];
            aiManager.setItemStack(variable, filter != null ? filter.getFilter() : drone.getInv().getStackInSlot(0).copy());
        }
        return super.getOutputWidget(drone, allWidgets);
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
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.ITEM_ASSIGN.get();
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
