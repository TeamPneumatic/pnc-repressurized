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
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class ProgWidgetPressureCondition extends ProgWidgetCondition {
    public static final MapCodec<ProgWidgetPressureCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            condParts(builder).apply(builder, ProgWidgetPressureCondition::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetPressureCondition> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            InvBaseFields.STREAM_CODEC, ProgWidgetInventoryBase::invBaseFields,
            ConditionFields.STREAM_CODEC, ProgWidgetCondition::conditionFields,
            ProgWidgetPressureCondition::new
    );

    public ProgWidgetPressureCondition() {
    }

    public ProgWidgetPressureCondition(PositionFields pos, InvBaseFields inv, ConditionFields cond) {
        super(pos, inv, cond);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetPressureCondition(getPosition(), invBaseFields().copy(), conditionFields());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDrone drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {

            @Override
            protected boolean evaluate(BlockPos pos) {
                BlockEntity te = drone.getDroneLevel().getBlockEntity(pos);
                if (te != null) {
                    float pressure = Float.MIN_VALUE;
                    for (Direction dir : DirectionUtil.VALUES) {
                        if (getSides()[dir.get3DDataValue()]) {
                            float p = PNCCapabilities.getAirHandler(te, dir)
                                    .map(IAirHandlerMachine::getPressure)
                                    .orElse(0f);
                            pressure = Math.max(pressure, p);
                        }
                    }
                    maybeRecordMeasuredVal(drone, (int) (pressure * 1000));
                    return ((ICondition) progWidget).getOperator().evaluate(pressure, ((ICondition) progWidget).getRequiredCount());
                }
                return false;
            }

        };
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_PRESSURE;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.CONDITION_PRESSURE.get();
    }
}
