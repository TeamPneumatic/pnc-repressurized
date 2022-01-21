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

package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIBlockInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ProgWidgetDigAndPlace extends ProgWidgetAreaItemBase implements IBlockOrdered, IMaxActions {
    private Ordering order;
    private int maxActions = 1;
    private boolean useMaxActions;

    @Override
    public Ordering getOrder() {
        return order;
    }

    @Override
    public void setOrder(Ordering order) {
        this.order = order;
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(xlate("pneumaticcraft.message.misc.order", xlate(order.getTranslationKey())));
    }

    ProgWidgetDigAndPlace(ProgWidgetType<?> type, Ordering order) {
        super(type);
        this.order = order;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putInt("order", order.ordinal());
        if (useMaxActions) tag.putBoolean("useMaxActions", true);
        tag.putInt("maxActions", maxActions);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        order = Ordering.values()[tag.getInt("order")];
        useMaxActions = tag.getBoolean("useMaxActions");
        maxActions = tag.getInt("maxActions");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeByte(order.ordinal());
        buf.writeBoolean(useMaxActions);
        buf.writeVarInt(maxActions);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        order = Ordering.values()[buf.readByte()];
        useMaxActions = buf.readBoolean();
        maxActions = buf.readVarInt();
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return Collections.singletonList(xlate(order.getTranslationKey()));
    }

    @Override
    public void setMaxActions(int maxActions) {
        this.maxActions = maxActions;
    }

    @Override
    public int getMaxActions() {
        return maxActions;
    }

    @Override
    public void setUseMaxActions(boolean useMaxActions) {
        this.useMaxActions = useMaxActions;
    }

    @Override
    public boolean useMaxActions() {
        return useMaxActions;
    }

    DroneAIBlockInteraction<?> setupMaxActions(DroneAIBlockInteraction<?> ai, IMaxActions widget) {
        return widget.useMaxActions() ? ai.setMaxActions(widget.getMaxActions()) : ai;
    }
}
