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

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetStandby extends ProgWidget implements IStandbyWidget {
    private boolean allowStandbyPickup;

    public ProgWidgetStandby() {
        super(ModProgWidgets.STANDBY.get());
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
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIStandby((DroneEntity) drone, (ProgWidget) widget);
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
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (allowStandbyPickup) tag.putBoolean("allowStandbyPickup", true);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        allowStandbyPickup = tag.getBoolean("allowStandbyPickup");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(allowStandbyPickup);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        allowStandbyPickup = buf.readBoolean();
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
