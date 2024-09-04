package me.desht.pneumaticcraft.common.util.chunkloading;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum PlayerLogoutTracker {
    INSTANCE;

    private final Map<UUID,Long> LOGGED_OUT = new HashMap<>();

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            LOGGED_OUT.put(sp.getUUID(), System.currentTimeMillis());
        }
    }

    /**
     * Get the time since the player logged out, in milliseconds
     * @param playerId the player's UUID
     * @return milliseconds since logout, 0 if player is logged in, or Long.MAX_VALUE if the player hasn't logged in since last server restart
     */
    public long getTimeSinceLogout(@Nullable MinecraftServer server, UUID playerId) {
        if (server == null) {
            return Long.MAX_VALUE;
        }
        if (server.getPlayerList().getPlayer(playerId) != null) {
            return 0L;
        }
        Long when = LOGGED_OUT.get(playerId);
        return when == null ? Long.MAX_VALUE : System.currentTimeMillis() - when;
    }

    public boolean isPlayerLoggedOutTooLong(MinecraftServer server, UUID playerId) {
        long offlineTime = ConfigHelper.common().drones.chunkloadOfflineTime.get() * 1000L;
        return offlineTime > 0L && PlayerLogoutTracker.INSTANCE.getTimeSinceLogout(server, playerId) > offlineTime;
    }
}
