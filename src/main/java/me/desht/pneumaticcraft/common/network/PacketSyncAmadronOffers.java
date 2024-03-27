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
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to sync up current Amadron offer list when the offer list changes (due to a shuffle or reload),
 * or when a player logs in
 */
public record PacketSyncAmadronOffers(Collection<AmadronOffer> activeOffers, boolean notifyPlayer) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("sync_amadron_offers");

    public static PacketSyncAmadronOffers create(boolean notifyPlayer) {
        return new PacketSyncAmadronOffers(AmadronOfferManager.getInstance().getActiveOffers(), notifyPlayer);
    }

    public static PacketSyncAmadronOffers fromNetwork(FriendlyByteBuf buf) {
        boolean notifyPlayer = buf.readBoolean();
        List<AmadronOffer>  activeOffers = new ArrayList<>();
        int offerCount = buf.readVarInt();
        for (int i = 0; i < offerCount; i++) {
            if (buf.readBoolean()) {
                activeOffers.add(AmadronPlayerOffer.playerOfferFromBuf(buf));
            } else {
                activeOffers.add(AmadronOffer.offerFromBuf(buf.readResourceLocation(), buf));
            }
        }
        return new PacketSyncAmadronOffers(activeOffers, notifyPlayer);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(notifyPlayer);
        buf.writeVarInt(activeOffers.size());
        for (AmadronOffer offer : activeOffers) {
            buf.writeBoolean(offer instanceof AmadronPlayerOffer);
            offer.write(buf);
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSyncAmadronOffers message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() ->
                AmadronOfferManager.getInstance().syncOffers(message.activeOffers(), message.notifyPlayer())
        );
    }

}
