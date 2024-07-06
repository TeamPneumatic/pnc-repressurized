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
import me.desht.pneumaticcraft.common.drone.ai.DroneAITeleport;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;

public class ProgWidgetTeleport extends ProgWidgetGoToLocation {
    public static final MapCodec<ProgWidgetTeleport> CODEC = RecordCodecBuilder.mapCodec(builder ->
        baseParts(builder).apply(builder, ProgWidgetTeleport::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetTeleport> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ProgWidgetTeleport::new
    );

    private ProgWidgetTeleport(PositionFields pos) {
        super(pos, false);
    }

    public ProgWidgetTeleport() {
        this(PositionFields.DEFAULT);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetTeleport(getPosition());
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.TELEPORT.get();
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_TELEPORT;
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return drone instanceof DroneEntity de ? new DroneAITeleport(de, (ProgWidget) widget) : null;
    }
}
