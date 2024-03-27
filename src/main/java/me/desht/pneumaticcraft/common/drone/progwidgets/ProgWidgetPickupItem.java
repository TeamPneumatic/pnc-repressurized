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

import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneEntityAIPickupItems;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

public class ProgWidgetPickupItem extends ProgWidgetAreaItemBase implements IItemPickupWidget {
    private boolean canSteal = false;

    public ProgWidgetPickupItem() {
        super(ModProgWidgets.PICKUP_ITEM.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_PICK_ITEM;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
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

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (canSteal) tag.putBoolean("canSteal", true);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        canSteal = tag.getBoolean("canSteal");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(canSteal);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        canSteal = buf.readBoolean();
    }
}
