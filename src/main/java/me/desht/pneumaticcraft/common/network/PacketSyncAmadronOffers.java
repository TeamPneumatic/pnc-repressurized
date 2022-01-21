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

import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to sync up current Amadron offer list when the offer list changes (due to a shuffle or reload),
 * or when a player logs in
 */
public class PacketSyncAmadronOffers {
    private final Collection<AmadronRecipe> activeOffers;
    private final boolean notifyPlayer;

    public PacketSyncAmadronOffers(boolean notifyPlayer) {
        this.notifyPlayer = notifyPlayer;
        this.activeOffers = AmadronOfferManager.getInstance().getActiveOffers();
    }

    public PacketSyncAmadronOffers(FriendlyByteBuf buf) {
        this.notifyPlayer = buf.readBoolean();
        this.activeOffers = new ArrayList<>();
        int offerCount = buf.readVarInt();
        for (int i = 0; i < offerCount; i++) {
            if (buf.readBoolean()) {
                activeOffers.add(AmadronPlayerOffer.playerOfferFromBuf(buf.readResourceLocation(), buf));
            } else {
                activeOffers.add(AmadronOffer.offerFromBuf(buf.readResourceLocation(), buf));
            }
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(notifyPlayer);
        buf.writeVarInt(activeOffers.size());
        for (AmadronRecipe offer : activeOffers) {
            buf.writeBoolean(offer instanceof AmadronPlayerOffer);
            buf.writeResourceLocation(offer.getId());
            offer.write(buf);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AmadronOfferManager.getInstance().syncOffers(activeOffers, notifyPlayer));
        ctx.get().setPacketHandled(true);
    }

}
