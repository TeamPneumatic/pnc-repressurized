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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIEntityExport;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.List;

public class ProgWidgetEntityExport extends ProgWidgetAreaItemBase {
    public static final MapCodec<ProgWidgetEntityExport> CODEC = RecordCodecBuilder.mapCodec(builder ->
        baseParts(builder).apply(builder, ProgWidgetEntityExport::new));

    public ProgWidgetEntityExport(PositionFields pos) {
        super(pos);
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.ENTITY_EXPORT.get();
    }

    public ProgWidgetEntityExport() {
        super(PositionFields.DEFAULT);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_ENTITY_EX;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.ORANGE;
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAIEntityExport<>(drone, (ProgWidgetAreaItemBase) widget);
    }
}
