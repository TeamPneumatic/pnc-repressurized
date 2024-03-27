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
import me.desht.pneumaticcraft.common.drone.ai.DroneEntityAIGoToLocation;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetGoToLocation extends ProgWidget implements IGotoWidget, IAreaProvider {
    private boolean doneWhenDeparting;

    public ProgWidgetGoToLocation() {
        super(ModProgWidgets.GOTO.get());
    }

    ProgWidgetGoToLocation(ProgWidgetType<ProgWidgetTeleport> type) {
        super(type);
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.area.error.noArea"));
        }
    }

    @Override
    public boolean doneWhenDeparting() {
        return doneWhenDeparting;
    }

    @Override
    public void setDoneWhenDeparting(boolean bool) {
        doneWhenDeparting = bool;
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.goto.doneWhen" + (doneWhenDeparting ? "Departing" : "Arrived")));
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_GOTO;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneEntityAIGoToLocation(drone, (ProgWidget) widget);
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
        return ImmutableList.of(ModProgWidgets.AREA.get());
    }

    @Override
    public void getArea(Set<BlockPos> area) {
        ProgWidgetAreaItemBase.getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[getParameters().size()]);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (doneWhenDeparting) tag.putBoolean("doneWhenDeparting", true);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        doneWhenDeparting = tag.getBoolean("doneWhenDeparting");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(doneWhenDeparting);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        doneWhenDeparting = buf.readBoolean();
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.LIGHT_BLUE;
    }
}
