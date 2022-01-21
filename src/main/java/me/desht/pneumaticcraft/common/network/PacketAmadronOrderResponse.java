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

import me.desht.pneumaticcraft.client.gui.GuiAmadron;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to confirm updated amount when player adjusts an offer with PacketAmadronOrderUpdate
 * (This needs a round trip rather than just updating client-side, since server needs to validate and cap
 *  requested shopping amounts)
 */
public class PacketAmadronOrderResponse {
    private final ResourceLocation offerId;
    private final int amount;

    public PacketAmadronOrderResponse(ResourceLocation offerId, int amount) {
        this.offerId = offerId;
        this.amount = amount;
    }

    public PacketAmadronOrderResponse(FriendlyByteBuf buf) {
        this.offerId = buf.readResourceLocation();
        this.amount = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(offerId);
        buf.writeVarInt(amount);
    }


    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ClientUtils.getClientPlayer();
            if (player.containerMenu instanceof ContainerAmadron) {
                ((ContainerAmadron) player.containerMenu).updateBasket(offerId, amount);
                GuiAmadron.basketUpdated();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
