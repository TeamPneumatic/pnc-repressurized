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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent from server so that all clients know the jet boots state (enabled/active) of all other clients.
 * Allows us to play particles, do rotations, etc. with minimum traffic.
 */
public class PacketJetBootsStateSync {
    private final UUID playerId;
    private final JetBootsStateTracker.JetBootsState state;

    public PacketJetBootsStateSync(Player player, JetBootsStateTracker.JetBootsState state) {
        this.playerId = player.getUUID();
        this.state = state;
    }

    PacketJetBootsStateSync(FriendlyByteBuf buf) {
        playerId = new UUID(buf.readLong(), buf.readLong());
        state = new JetBootsStateTracker.JetBootsState(buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(playerId.getMostSignificantBits());
        buf.writeLong(playerId.getLeastSignificantBits());
        buf.writeBoolean(state.isEnabled());
        buf.writeBoolean(state.isActive());
        buf.writeBoolean(state.isBuilderMode());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> JetBootsStateTracker.getTracker(ClientUtils.getClientPlayer()).setJetBootsState(playerId, state));
        ctx.get().setPacketHandled(true);
    }

}
