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

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker.JetBootsState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.UUID;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent from server so that all clients know the jet boots state (enabled/active) of all other clients.
 * Allows us to play particles, do rotations, etc. with minimum traffic.
 */
public record PacketJetBootsStateSync(UUID playerId, JetBootsState state) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("jetboots_state_sync");

    public static PacketJetBootsStateSync fromNetwork(FriendlyByteBuf buf) {
        return new PacketJetBootsStateSync(buf.readUUID(), JetBootsState.fromNetwork(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        state.toNetwork(buf);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketJetBootsStateSync message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() ->
                JetBootsStateTracker.getTracker(ClientUtils.getClientPlayer())
                        .syncFromServer(message.playerId(), message.state()));
    }

}
