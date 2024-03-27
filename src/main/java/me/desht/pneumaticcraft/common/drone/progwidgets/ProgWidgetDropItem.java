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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIDropItem;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
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

public class ProgWidgetDropItem extends ProgWidgetInventoryBase implements IItemDropper {
    private boolean dropStraight;
    private boolean pickupDelay = true;

    public ProgWidgetDropItem() {
        super(ModProgWidgets.DROP_ITEM.get());
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
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (dropStraight) tag.putBoolean("dropStraight", true);
        if (pickupDelay) tag.putBoolean("pickupDelay", true);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        dropStraight = tag.getBoolean("dropStraight");
        pickupDelay = tag.getBoolean("pickupDelay");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(dropStraight);
        buf.writeBoolean(pickupDelay);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        dropStraight = buf.readBoolean();
        pickupDelay = buf.readBoolean();
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
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIDropItem(drone, (ProgWidgetInventoryBase) widget);
    }

    @Override
    protected boolean isUsingSides() {
        return false;
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return Collections.singletonList(xlate("pneumaticcraft.gui.progWidget.drop.dropMethod." + (dropStraight() ? "straight" : "random")));
    }
}
