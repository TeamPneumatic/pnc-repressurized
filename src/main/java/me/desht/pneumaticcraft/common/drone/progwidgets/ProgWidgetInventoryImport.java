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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.ai.DroneEntityAIInventoryImport;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

public class ProgWidgetInventoryImport extends ProgWidgetInventoryBase {

    public static final MapCodec<ProgWidgetInventoryImport> CODEC = RecordCodecBuilder.mapCodec(builder ->
            invParts(builder).apply(builder, ProgWidgetInventoryImport::new));

    public ProgWidgetInventoryImport(PositionFields pos, InvBaseFields invBaseFields) {
        super(pos, invBaseFields);
    }

    public ProgWidgetInventoryImport() {
        super(PositionFields.DEFAULT, InvBaseFields.DEFAULT);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_INV_IM;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.INVENTORY_IMPORT.get();
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneEntityAIInventoryImport(drone, (ProgWidgetInventoryBase) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.BLUE;
    }
}
