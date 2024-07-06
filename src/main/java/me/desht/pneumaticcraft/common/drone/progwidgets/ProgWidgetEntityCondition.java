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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class ProgWidgetEntityCondition extends ProgWidgetCondition {
    public static final MapCodec<ProgWidgetEntityCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            condParts(builder).apply(builder, ProgWidgetEntityCondition::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetEntityCondition> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            InvBaseFields.STREAM_CODEC, ProgWidgetInventoryBase::invBaseFields,
            ConditionFields.STREAM_CODEC, ProgWidgetCondition::conditionFields,
            ProgWidgetEntityCondition::new
    );

    public ProgWidgetEntityCondition() {
    }

    public ProgWidgetEntityCondition(PositionFields pos, InvBaseFields inv, ConditionFields cond) {
        super(pos, inv, cond);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetEntityCondition(getPosition(), invBaseFields().copy(), conditionFields());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.TEXT.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDrone drone, IProgWidget widget) {
        return null;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.CONDITION_ENTITY.get();
    }

    @Override
    public IProgWidget getOutputWidget(IDrone drone, List<IProgWidget> allWidgets) {
        List<Entity> entities = getValidEntities(drone.getDroneLevel());
        boolean result = getOperator() == Operator.EQ ? entities.size() == getRequiredCount() : entities.size() >= getRequiredCount();
        if (result) {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.condition.evaluatedTrue");
        } else {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.condition.evaluatedFalse");
        }
        maybeRecordMeasuredVal(drone, entities.size());
        return ProgWidgetJump.jumpToLabel(drone, allWidgets, this, result);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_ENTITY;
    }
}
