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

import me.desht.pneumaticcraft.common.inventory.AmadronMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent from client when an offer widget is clicked in the Amadron GUI to update the server-side order amount
 */
public record PacketAmadronOrderUpdate(ResourceLocation orderId, int mouseButton, boolean sneaking) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("amadron_order_update");

    public static PacketAmadronOrderUpdate fromNetwork(FriendlyByteBuf buffer) {
        return new PacketAmadronOrderUpdate(buffer.readResourceLocation(), buffer.readByte(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(orderId);
        buf.writeByte(mouseButton);
        buf.writeBoolean(sneaking);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketAmadronOrderUpdate message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player instanceof ServerPlayer sp && player.containerMenu instanceof AmadronMenu menu) {
                menu.clickOffer(message.orderId(), message.mouseButton(), message.sneaking(), sp);
            }
        }));
    }
}
