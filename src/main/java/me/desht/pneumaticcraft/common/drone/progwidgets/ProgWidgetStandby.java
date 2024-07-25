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
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetStandby extends ProgWidget implements IStandbyWidget {
    public static final MapCodec<ProgWidgetStandby> CODEC = RecordCodecBuilder.mapCodec(builder ->
        baseParts(builder).and(Codec.BOOL.optionalFieldOf("allow_pickup", false).forGetter(ProgWidgetStandby::allowPickupOnStandby)
    ).apply(builder, ProgWidgetStandby::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetStandby> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.BOOL, ProgWidgetStandby::allowPickupOnStandby,
            ProgWidgetStandby::new
    );

    private boolean allowStandbyPickup;

    public ProgWidgetStandby(PositionFields pos, boolean allowStandbyPickup) {
        super(pos);
        this.allowStandbyPickup = allowStandbyPickup;
    }

    public ProgWidgetStandby() {
        this(PositionFields.DEFAULT, false);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetStandby(getPosition(), allowStandbyPickup);
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.LIME;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_STANDBY;
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAIStandby((DroneEntity) drone, (ProgWidget) widget);
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.STANDBY.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);

        if (allowStandbyPickup) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.standby.allowPickup"));
        }
    }

    @Override
    public boolean allowPickupOnStandby() {
        return allowStandbyPickup;
    }

    @Override
    public void setAllowStandbyPickup(boolean allowStandbyPickup) {
        this.allowStandbyPickup = allowStandbyPickup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProgWidgetStandby that = (ProgWidgetStandby) o;
        return baseEquals(that) && allowStandbyPickup == that.allowStandbyPickup;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), allowStandbyPickup);
    }

    public static class DroneAIStandby extends Goal {
        private final DroneEntity drone;
        private final ProgWidget widget;

        DroneAIStandby(DroneEntity drone, ProgWidget widget) {
            this.drone = drone;
            this.widget = widget;
        }

        @Override
        public boolean canUse() {
            boolean allowPickup = widget instanceof IStandbyWidget s && s.allowPickupOnStandby();
            drone.setStandby(true, allowPickup);
            return false;
        }
    }
}
