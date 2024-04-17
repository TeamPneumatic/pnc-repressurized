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
import me.desht.pneumaticcraft.common.recipes.amadron.OfferType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 * Sent by client to request Amadron offer sync when ready (i.e. client has a level)
 * Sent by server to sync up current Amadron offer list when the offer list changes (due to a shuffle or reload),
 * or when client requests it
 */
public record PacketSyncAmadronOffers(Collection<AmadronOffer> activeOffers, boolean notifyPlayer) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("sync_amadron_offers");

    public static PacketSyncAmadronOffers createRequest() {
        return new PacketSyncAmadronOffers(List.of(), false);
    }

    public static PacketSyncAmadronOffers create(boolean notifyPlayer) {
        return new PacketSyncAmadronOffers(AmadronOfferManager.getInstance().getActiveOffers(), notifyPlayer);
    }

    public static PacketSyncAmadronOffers fromNetwork(FriendlyByteBuf buf) {
        boolean notifyPlayer = buf.readBoolean();
        List<AmadronOffer>  activeOffers = new ArrayList<>();
        int offerCount = buf.readVarInt();
        for (int i = 0; i < offerCount; i++) {
            OfferType type = buf.readEnum(OfferType.class);
            type.read(buf).ifPresent(activeOffers::add);
        }
        return new PacketSyncAmadronOffers(activeOffers, notifyPlayer);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(notifyPlayer);
        buf.writeVarInt(activeOffers.size());
        for (AmadronOffer offer : activeOffers) {
            buf.writeEnum(offer.getOfferType());
            offer.getOfferType().write(buf, offer);
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSyncAmadronOffers message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(
                () -> {
                    if (ctx.flow().isClientbound()) {
                        AmadronOfferManager.getInstance().syncOffers(message.activeOffers(), message.notifyPlayer());
                    } else {
                        ctx.player().ifPresent(player -> {
                            if (player instanceof ServerPlayer sp) {
                                NetworkHandler.sendNonLocal(sp, PacketSyncAmadronOffers.create(false));
                            }
                        });
                    }
                }
        );
    }
}
