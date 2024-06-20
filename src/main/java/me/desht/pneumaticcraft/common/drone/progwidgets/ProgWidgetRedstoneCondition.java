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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ProgWidgetRedstoneCondition extends ProgWidgetCondition {
    public static final MapCodec<ProgWidgetRedstoneCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            condParts(builder).apply(builder, ProgWidgetRedstoneCondition::new));

    public ProgWidgetRedstoneCondition() {
    }

    public ProgWidgetRedstoneCondition(PositionFields pos, InvBaseFields inv, ConditionFields cond) {
        super(pos, inv, cond);
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_REDSTONE;
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDrone drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {
            @Override
            protected boolean evaluate(BlockPos pos) {
                int redstoneLevel = drone.getDroneLevel().getBestNeighborSignal(pos);
                int requiredRedstone = ((ICondition) progWidget).getRequiredCount();
                maybeRecordMeasuredVal(drone, redstoneLevel);
                return ((ICondition) progWidget).getOperator().evaluate(redstoneLevel, requiredRedstone);
            }
        };
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.CONDITION_REDSTONE.get();
    }
}
