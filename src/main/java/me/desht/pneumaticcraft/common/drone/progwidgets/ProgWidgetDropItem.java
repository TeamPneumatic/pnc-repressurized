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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIDropItem;
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

public class ProgWidgetDropItem extends ProgWidgetInventoryBase implements IItemDropper {
    public static final MapCodec<ProgWidgetDropItem> CODEC = RecordCodecBuilder.mapCodec(builder ->
            invParts(builder).and(builder.group(
                    Codec.BOOL.optionalFieldOf("drop_straight", false).forGetter(ProgWidgetDropItem::dropStraight),
                    Codec.BOOL.optionalFieldOf("pick_delay", false).forGetter(ProgWidgetDropItem::hasPickupDelay)
            )
    ).apply(builder, ProgWidgetDropItem::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetDropItem> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            InvBaseFields.STREAM_CODEC, ProgWidgetInventoryBase::invBaseFields,
            ByteBufCodecs.BOOL, ProgWidgetDropItem::dropStraight,
            ByteBufCodecs.BOOL, ProgWidgetDropItem::hasPickupDelay,
            ProgWidgetDropItem::new
    );

    private boolean dropStraight;
    private boolean pickupDelay;

    public ProgWidgetDropItem(PositionFields pos, InvBaseFields invBaseFields, boolean dropStraight, boolean pickupDelay) {
        super(pos, invBaseFields);
        this.dropStraight = dropStraight;
        this.pickupDelay = pickupDelay;
    }

    public ProgWidgetDropItem() {
        super(PositionFields.DEFAULT, InvBaseFields.DEFAULT);

        this.dropStraight = false;
        this.pickupDelay = true;
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetDropItem(getPosition(), invBaseFields(), dropStraight, pickupDelay);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.MAGENTA;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_DROP_ITEM;
    }

    @Override
    public boolean dropStraight() {
        return dropStraight;
    }

    @Override
    public void setDropStraight(boolean dropStraight) {
        this.dropStraight = dropStraight;
    }

    @Override
    public boolean hasPickupDelay() {
        return pickupDelay;
    }

    @Override
    public void setPickupDelay(boolean pickupDelay) {
        this.pickupDelay = pickupDelay;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.DROP_ITEM.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        if (pickupDelay) {
            curTooltip.add(Component.translatable("pneumaticcraft.gui.progWidget.drop.hasPickupDelay"));
        } else {
            curTooltip.add(Component.translatable("pneumaticcraft.gui.progWidget.drop.noPickupDelay"));
        }
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAIDropItem(drone, (ProgWidgetInventoryBase) widget);
    }

    @Override
    protected boolean isUsingSides() {
        return false;
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return List.of(xlate("pneumaticcraft.gui.progWidget.drop.dropMethod." + (dropStraight() ? "straight" : "random")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgWidgetDropItem that = (ProgWidgetDropItem) o;
        return baseEquals(that) && dropStraight == that.dropStraight && pickupDelay == that.pickupDelay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), dropStraight, pickupDelay);
    }
}
