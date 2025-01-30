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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIPlace;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

public class ProgWidgetPlace extends ProgWidgetDigAndPlace {
    public static final MapCodec<ProgWidgetPlace> CODEC = RecordCodecBuilder.mapCodec(builder ->
            digPlaceParts(builder).and(
                    Codec.BOOL.optionalFieldOf("randomize", false).forGetter(ProgWidgetPlace::isRandomize)
            ).apply(builder, ProgWidgetPlace::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetPlace> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            DigPlaceFields.STREAM_CODEC, p -> p.digPlaceFields,
            ByteBufCodecs.BOOL, ProgWidgetPlace::isRandomize,
            ProgWidgetPlace::new
    );

    private boolean randomize;

    public ProgWidgetPlace(PositionFields pos, DigPlaceFields digPlaceFields, boolean randomize) {
        super(pos, digPlaceFields);
        this.randomize = randomize;
    }

    public ProgWidgetPlace() {
        super(PositionFields.DEFAULT, DigPlaceFields.makeDefault(Ordering.LOW_TO_HIGH));
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetPlace(getPosition(), digPlaceFields, randomize);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_PLACE;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.PLACE.get();
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIPlace<>(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.YELLOW;
    }

    public boolean isRandomize() {
        return randomize;
    }

    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }
}
