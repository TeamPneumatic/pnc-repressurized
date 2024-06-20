package me.desht.pneumaticcraft.api.drone.debug;

import me.desht.pneumaticcraft.api.drone.IDrone;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * Provides access to the drone debugger. Retrieve an instance of this with {@link IDrone#getDebugger()}
 */
@ApiStatus.NonExtendable
public interface IDroneDebugger {
    void addEntry(String s);

    void addEntry(String s, BlockPos pos);

    void addEntry(DroneDebugEntry entry);

    Collection<ServerPlayer> getDebuggingPlayers();

    DroneDebugEntry getDebugEntry(int widgetIndex);

    void trackAsDebugged(ServerPlayer player);
}
