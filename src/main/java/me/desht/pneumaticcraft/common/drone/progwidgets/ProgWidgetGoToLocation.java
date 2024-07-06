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
import me.desht.pneumaticcraft.common.drone.ai.DroneEntityAIGoToLocation;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetGoToLocation extends ProgWidget implements IGotoWidget, IAreaProvider {
    public static final MapCodec<ProgWidgetGoToLocation> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(
                    Codec.BOOL.optionalFieldOf("done_when_depart", false).forGetter(ProgWidgetGoToLocation::doneWhenDeparting)
            ).apply(builder, ProgWidgetGoToLocation::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetGoToLocation> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.BOOL, ProgWidgetGoToLocation::doneWhenDeparting,
            ProgWidgetGoToLocation::new
    );

    private boolean doneWhenDeparting;

    protected ProgWidgetGoToLocation(PositionFields pos, boolean doneWhenDeparting) {
        super(pos);
        this.doneWhenDeparting = doneWhenDeparting;
    }

    public ProgWidgetGoToLocation() {
        this(PositionFields.DEFAULT, false);
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
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.GOTO.get();
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
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
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
        return ImmutableList.of(ModProgWidgetTypes.AREA.get());
    }

    @Override
    public void getArea(Set<BlockPos> area) {
        ProgWidgetAreaItemBase.getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[getParameters().size()]);
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.LIGHT_BLUE;
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetGoToLocation(getPosition(), doneWhenDeparting);
    }
}
