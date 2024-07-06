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
import me.desht.pneumaticcraft.common.drone.ai.DroneEntityAIPickupItems;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

public class ProgWidgetPickupItem extends ProgWidgetAreaItemBase implements IItemPickupWidget {
    public static final MapCodec<ProgWidgetPickupItem> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(Codec.BOOL.optionalFieldOf("can_steal", false).forGetter(ProgWidgetPickupItem::canSteal)
            ).apply(builder, ProgWidgetPickupItem::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetPickupItem> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.BOOL, ProgWidgetPickupItem::canSteal,
            ProgWidgetPickupItem::new
    );

    private boolean canSteal;

    private ProgWidgetPickupItem(PositionFields pos, boolean canSteal) {
        super(pos);

        this.canSteal = canSteal;
    }

    public ProgWidgetPickupItem() {
        this(PositionFields.DEFAULT, false);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetPickupItem(getPosition(), canSteal);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_PICK_ITEM;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.PICKUP_ITEM.get();
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneEntityAIPickupItems(drone, (ProgWidgetAreaItemBase) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PINK;
    }

    @Override
    public boolean canSteal() {
        return canSteal;
    }

    @Override
    public void setCanSteal(boolean canSteal) {
        this.canSteal = canSteal;
    }

}
