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

import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when an order is purchased by someone, to update remaining stock levels as seen on clients.
 */
public class PacketAmadronStockUpdate {
    private final ResourceLocation id;
    private final int stock;

    public PacketAmadronStockUpdate(ResourceLocation id, int stock) {
        this.id = id;
        this.stock = stock;
    }

    public PacketAmadronStockUpdate(FriendlyByteBuf buffer) {
        this.id = buffer.readResourceLocation();
        this.stock = buffer.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeVarInt(stock);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() == null) {
                AmadronOfferManager.getInstance().updateStock(id, stock);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
