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

import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent from client when an offer widget is clicked in the Amadron GUI to update the server-side order amount
 */
public class PacketAmadronOrderUpdate {
    private final ResourceLocation orderId;
    private final int mouseButton;
    private final boolean sneaking;

    public PacketAmadronOrderUpdate(ResourceLocation orderId, int mouseButton, boolean sneaking) {
        this.orderId = orderId;
        this.mouseButton = mouseButton;
        this.sneaking = sneaking;
    }

    public PacketAmadronOrderUpdate(FriendlyByteBuf buffer) {
        orderId = buffer.readResourceLocation();
        mouseButton = buffer.readByte();
        sneaking = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(orderId);
        buf.writeByte(mouseButton);
        buf.writeBoolean(sneaking);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof ContainerAmadron) {
                ((ContainerAmadron) player.containerMenu).clickOffer(orderId, mouseButton, sneaking, player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
