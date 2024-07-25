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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIHarvest;
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

public class ProgWidgetHarvest extends ProgWidgetDigAndPlace implements IToolUser {
    public static final MapCodec<ProgWidgetHarvest> CODEC = RecordCodecBuilder.mapCodec(builder ->
            digPlaceParts(builder).and(
                    Codec.BOOL.optionalFieldOf("require_hoe", false).forGetter(ProgWidgetHarvest::requiresTool)
            ).apply(builder, ProgWidgetHarvest::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetHarvest> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            DigPlaceFields.STREAM_CODEC, p -> p.digPlaceFields,
            ByteBufCodecs.BOOL, ProgWidgetHarvest::requiresTool,
            ProgWidgetHarvest::new
    );

    private boolean requireHoe;

    public ProgWidgetHarvest(PositionFields pos, DigPlaceFields digPlaceFields, boolean requireHoe) {
        super(pos, digPlaceFields);

        this.requireHoe = requireHoe;
    }

    public ProgWidgetHarvest() {
        super(PositionFields.DEFAULT, DigPlaceFields.makeDefault(Ordering.CLOSEST));
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetHarvest(getPosition(), digPlaceFields, requireHoe);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_HARVEST;
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIHarvest(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.BROWN;
    }

    @Override
    public boolean requiresTool(){
        return requireHoe;
    }
    
    @Override
    public void setRequiresTool(boolean requireHoe){
        this.requireHoe = requireHoe;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.HARVEST.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        
        if (requiresTool()) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.harvest.requiresHoe"));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProgWidgetHarvest that = (ProgWidgetHarvest) o;
        return baseEquals(that) && requireHoe == that.requireHoe;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), requireHoe);
    }
}
