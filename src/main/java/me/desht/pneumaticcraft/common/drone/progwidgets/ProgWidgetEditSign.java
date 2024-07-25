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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIEditSign;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.variables.TextVariableParser;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProgWidgetEditSign extends ProgWidgetAreaItemBase implements ISignEditWidget {
    public static final MapCodec<ProgWidgetEditSign> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(Codec.BOOL.optionalFieldOf("back_side", false).forGetter(ProgWidgetEditSign::isSignBackSide)
    ).apply(builder, ProgWidgetEditSign::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetEditSign> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.BOOL, ProgWidgetEditSign::isSignBackSide,
            ProgWidgetEditSign::new
    );

    private boolean backSide;

    public ProgWidgetEditSign(PositionFields pos, boolean backSide) {
        super(pos);
        this.backSide = backSide;
    }

    public ProgWidgetEditSign() {
        super(PositionFields.DEFAULT);

        backSide = false;
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetEditSign(getPosition(), isSignBackSide());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_EDIT_SIGN;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.EDIT_SIGN.get();
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAIEditSign(drone, (ProgWidgetAreaItemBase) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PURPLE;
    }

    @Override
    public String[] getLines() {
        List<String> lines = new ArrayList<>();
        ProgWidgetText textWidget = (ProgWidgetText) getConnectedParameters()[1];
        while (textWidget != null) {
            lines.add(new TextVariableParser(textWidget.string, aiManager).parse());
            textWidget = (ProgWidgetText) textWidget.getConnectedParameters()[0];
        }
        return lines.toArray(new String[0]);
    }

    @Override
    public boolean canSetParameter(int index) {
        return index != 3;
    }

    @Override
    public boolean isSignBackSide() {
        return backSide;
    }

    @Override
    public void setSignBackSide(boolean backSide) {
        this.backSide = backSide;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgWidgetEditSign that = (ProgWidgetEditSign) o;
        return baseEquals(that) && backSide == that.backSide;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), backSide);
    }
}
