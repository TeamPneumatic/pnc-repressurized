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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.variables.TextVariableParser;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetRename extends ProgWidget implements IRenamingWidget, IVariableWidget {
    public static final MapCodec<ProgWidgetRename> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).apply(builder, ProgWidgetRename::new));

    private DroneAIManager aiManager;

    public ProgWidgetRename(PositionFields pos) {
        super(pos);
    }

    public ProgWidgetRename() {
        super(PositionFields.DEFAULT);
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.RENAME.get();
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
        return ImmutableList.of(ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_RENAME;
    }

    @Override
    public Goal getWidgetAI(final IDrone drone, final IProgWidget widget) {
        return new DroneAIRename(drone, (IRenamingWidget) widget);
    }

    @Override
    public String getNewName() {
        return getConnectedParameters()[0] != null ? new TextVariableParser(((ProgWidgetText) getConnectedParameters()[0]).string, aiManager).parse() : null;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    @Override
    public void addVariables(Set<String> variables) {
    }

    private static class DroneAIRename extends Goal {
        private final IDrone drone;
        private final IRenamingWidget widget;

        DroneAIRename(IDrone drone, IRenamingWidget widget) {
            this.drone = drone;
            this.widget = widget;
        }

        @Override
        public boolean canUse() {
            drone.setName(widget.getNewName() != null ? Component.literal(widget.getNewName()) : xlate("entity.pneumaticcraft.drone"));
            return false;
        }
    }

}
