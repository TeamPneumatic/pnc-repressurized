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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when an order is purchased by someone, to update remaining stock levels as seen on clients.
 */
public record PacketAmadronStockUpdate(ResourceLocation id, int stock) implements CustomPacketPayload {
    public static final Type<PacketAmadronStockUpdate> TYPE = new Type<>(RL("amadron_stock_update"));

    public static final StreamCodec<FriendlyByteBuf, PacketAmadronStockUpdate> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, PacketAmadronStockUpdate::id,
            ByteBufCodecs.VAR_INT, PacketAmadronStockUpdate::stock,
            PacketAmadronStockUpdate::new
    );

    @Override
    public Type<PacketAmadronStockUpdate> type() {
        return TYPE;
    }

    public static void handle(PacketAmadronStockUpdate message, IPayloadContext ctx) {
        AmadronOfferManager.getInstance().updateStock(message.id(), message.stock());
    }
}
