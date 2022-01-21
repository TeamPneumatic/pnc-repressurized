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

package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client from area tool GUI to update stored settings
 */
public class PacketUpdateGPSAreaTool {
    private CompoundTag areaWidgetData;
    private InteractionHand hand;

    public PacketUpdateGPSAreaTool(ProgWidgetArea area, InteractionHand hand) {
        this.hand = hand;
        this.areaWidgetData = new CompoundTag();
        area.writeToNBT(areaWidgetData);
    }

    public PacketUpdateGPSAreaTool(FriendlyByteBuf buffer) {
        try {
            areaWidgetData = buffer.readNbt();
            hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        try {
            buffer.writeNbt(areaWidgetData);
            buffer.writeBoolean(hand == InteractionHand.MAIN_HAND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                ItemStack stack = ctx.get().getSender().getItemInHand(hand);
                if (stack.getItem() == ModItems.GPS_AREA_TOOL.get()) {
                    stack.setTag(areaWidgetData);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
