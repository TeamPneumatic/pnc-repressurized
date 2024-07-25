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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIDig;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetDig extends ProgWidgetDigAndPlace implements IToolUser {
    public static final MapCodec<ProgWidgetDig> CODEC = RecordCodecBuilder.mapCodec(builder ->
            digPlaceParts(builder).and(
                    Codec.BOOL.optionalFieldOf("require_tool", false).forGetter(ProgWidgetDig::requiresTool)
            ).apply(builder, ProgWidgetDig::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetDig> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            DigPlaceFields.STREAM_CODEC, p -> p.digPlaceFields,
            ByteBufCodecs.BOOL, ProgWidgetDig::requiresTool,
            ProgWidgetDig::new
    );

    private boolean requireDiggingTool;

    public ProgWidgetDig(PositionFields pos, DigPlaceFields digPlaceFields, boolean requireDiggingTool) {
        super(pos, digPlaceFields);

        this.requireDiggingTool = requireDiggingTool;
    }

    public ProgWidgetDig() {
        super(PositionFields.DEFAULT, DigPlaceFields.makeDefault(Ordering.CLOSEST));
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetDig(getPosition(), digPlaceFields, requireDiggingTool);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_DIG;
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIDig(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.BROWN;
    }

    @Override
    public boolean requiresTool(){
        return requireDiggingTool;
    }

    @Override
    public void setRequiresTool(boolean requireDiggingTool){
        this.requireDiggingTool = requireDiggingTool;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.DIG.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);

        if (requiresTool()) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.dig.requiresDiggingTool"));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgWidgetDig that = (ProgWidgetDig) o;
        return baseEquals(that) && requireDiggingTool == that.requireDiggingTool;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), requireDiggingTool);
    }
}
