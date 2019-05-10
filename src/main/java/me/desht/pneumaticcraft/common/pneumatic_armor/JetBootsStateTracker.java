package me.desht.pneumaticcraft.common.pneumatic_armor;

import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketJetBootsStateSync;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JetBootsStateTracker {
    private static final JetBootsStateTracker clientTracker = new JetBootsStateTracker();
    private static final JetBootsStateTracker serverTracker = new JetBootsStateTracker();

    private final Map<UUID, JetBootsState> stateMap = new HashMap<>();

    private static JetBootsStateTracker getClientTracker() {
        return clientTracker;
    }

    private static JetBootsStateTracker getServerTracker() {
        return serverTracker;
    }

    public static JetBootsStateTracker getTracker(EntityPlayer player) {
        return player.world.isRemote ? getClientTracker() : getServerTracker();
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
    void setJetBootsState(EntityPlayer player, boolean enabled, boolean active, boolean builderMode) {
        if (!player.world.isRemote) {
            JetBootsState state = stateMap.computeIfAbsent(player.getUniqueID(), uuid -> new JetBootsState(false, false, false));

            boolean sendPacket = state.enabled != enabled || state.active != active || state.builderMode != builderMode;
            state.enabled = enabled;
            state.active = active;
            state.builderMode = builderMode;
            if (sendPacket) NetworkHandler.sendToDimension(new PacketJetBootsStateSync(player, state), player.world.provider.getDimension());
        }
    }

    /**
     * Set jet boots state client-side; only called from PacketJetBootsStateSync packet handler.
     *
     * @param playerId a player's UUID (not necessarily the client player; could be another player in this dimension)
     * @param state full jet boots state
     */
    public void setJetBootsState(UUID playerId, JetBootsState state) {
        stateMap.put(playerId, state);
    }

    public boolean areJetBootsEnabled(EntityPlayer player) {
        JetBootsState state = stateMap.get(player.getUniqueID());
        return state != null && state.enabled;
    }

    public boolean areJetBootsActive(EntityPlayer player) {
        JetBootsState state = stateMap.get(player.getUniqueID());
        return state != null && state.active;
    }

    public boolean isBuilderMode(EntityPlayer player) {
        JetBootsState state = stateMap.get(player.getUniqueID());
        return state != null && state.builderMode;
    }

    public JetBootsState getJetBootsState(EntityPlayer player) {
        return stateMap.getOrDefault(player.getUniqueID(), new JetBootsState(false, false, false));
    }

    public static class JetBootsState {
        private boolean enabled;  // switched on
        private boolean active;   // actively firing (player holding Jump key)
        private boolean builderMode; // player in builder mode (prevents model rotation)

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
