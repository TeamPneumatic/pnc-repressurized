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

package me.desht.pneumaticcraft.common.pneumatic_armor;

import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketJetBootsStateSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JetBootsStateTracker {
    private static final JetBootsStateTracker clientTracker = new JetBootsStateTracker();
    private static final JetBootsStateTracker serverTracker = new JetBootsStateTracker();

    private final Map<UUID, JetBootsState> stateMap = new HashMap<>();

    public static JetBootsStateTracker getClientTracker() {
        return clientTracker;
    }

    public static JetBootsStateTracker getServerTracker() {
        return serverTracker;
    }

    public static JetBootsStateTracker getTracker(Player player) {
        return player.level().isClientSide ? getClientTracker() : getServerTracker();
    }

    private JetBootsStateTracker() {
    }

    /**
     * Set jet boots state server-side.  No-op if called client-side (client state is updated by the
     * PacketJetBootsStateSync packet).
     *
     * @param player the player
     * @param enabled jet boots switched on?
     * @param active jet boots firing?
     * @param builderMode in builder mode?
     */
    public void setJetBootsState(Player player, boolean enabled, boolean active, boolean builderMode) {
        if (!player.level().isClientSide) {
            JetBootsState state = stateMap.computeIfAbsent(player.getUUID(), uuid -> new JetBootsState(false, false, false));

            boolean sendPacket = state.enabled != enabled || state.active != active || state.builderMode != builderMode;
            state.enabled = enabled;
            state.active = active;
            state.builderMode = builderMode;
            if (sendPacket) {
                NetworkHandler.sendToAllTracking(new PacketJetBootsStateSync(player.getUUID(), state), player.level(), player.blockPosition());
            }
        }
    }

    /**
     * Set jet boots state client-side; only called from PacketJetBootsStateSync packet handler.
     *
     * @param playerId a player's UUID (not necessarily the client player; could be another player in this dimension)
     * @param state full jet boots state
     */
    public void syncFromServer(UUID playerId, JetBootsState state) {
        stateMap.put(playerId, state);
    }

    public JetBootsState getJetBootsState(Player player) {
        return stateMap.getOrDefault(player.getUUID(), new JetBootsState(false, false, false));
    }

    /**
     * Synced state: set on the server and sync'd to clients (i.e. other players need to know what this player's state is)
     */
    public static class JetBootsState {
        private boolean enabled;  // switched on
        private boolean active;   // actively firing (player holding thrust key)
        private boolean builderMode; // player in builder mode (prevents model rotation)

        public static StreamCodec<FriendlyByteBuf, JetBootsState> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, JetBootsState::isEnabled,
                ByteBufCodecs.BOOL, JetBootsState::isActive,
                ByteBufCodecs.BOOL, JetBootsState::isBuilderMode,
                JetBootsState::new
        );

        public JetBootsState(boolean enabled, boolean active, boolean builderMode) {
            this.enabled = enabled;
            this.active = active;
            this.builderMode = builderMode;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isActive() {
            return active;
        }

        public boolean isBuilderMode() {
            return builderMode;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public void setBuilderMode(boolean builderMode) {
            this.builderMode = builderMode;
        }

        public boolean shouldRotatePlayer() {
            return enabled && active && !builderMode;
        }

        @Override
        public String toString() {
            return String.format("[en=%b,ac=%b,bu=%b]", enabled, active, builderMode);
        }
    }
}
