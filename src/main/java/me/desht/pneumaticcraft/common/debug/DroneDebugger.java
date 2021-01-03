package me.desht.pneumaticcraft.common.debug;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSendDroneDebugEntry;
import me.desht.pneumaticcraft.common.network.PacketSyncDroneEntityProgWidgets;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class DroneDebugger {
    private final IDroneBase drone;
    private final DroneDebugList debugList = new DroneDebugList();
    private final Set<ServerPlayerEntity> debuggingPlayers = new HashSet<>();  // players who receive debug data

    public DroneDebugger(IDroneBase drone) {
        this.drone = drone;
    }

    public DroneDebugEntry getDebugEntry(int widgetID) {
        return debugList.get(widgetID);
    }

    public void addEntry(String message) {
        addEntry(message, null);
    }

    public void addEntry(String message, BlockPos pos) {
        DroneDebugEntry entry = new DroneDebugEntry(message, getActiveWidgetIndex(), pos);

        // add the entry server-side
        addEntry(entry);

        // add the entry client-side
        PacketSendDroneDebugEntry packet = new PacketSendDroneDebugEntry(entry, drone);
        for (ServerPlayerEntity player : debuggingPlayers) {
            NetworkHandler.sendToPlayer(packet, player);
        }
    }

    public void addEntry(DroneDebugEntry entry) {
        debugList.addEntry(entry);
    }

    public void trackAsDebugged(ServerPlayerEntity player) {
        NetworkHandler.sendToPlayer(new PacketSyncDroneEntityProgWidgets(drone), player);

        for (DroneDebugEntry entry : debugList.getAll()) {
            NetworkHandler.sendToPlayer(new PacketSendDroneDebugEntry(entry, drone), player);
        }

        debuggingPlayers.add(player);
    }

    public void updateDebuggingPlayers() {
        debuggingPlayers.removeIf(player -> !player.isAlive() || !ItemPneumaticArmor.isPlayerDebuggingDrone(player, drone));
    }

    private int getActiveWidgetIndex() {
        return drone.getActiveWidgetIndex();
    }

    private class DroneDebugList {
        private final Map<Integer, DroneDebugEntry> debugEntries = new HashMap<>();

        private DroneDebugList() {
        }

        void addEntry(DroneDebugEntry entry) {
            debugEntries.put(DroneDebugger.this.getActiveWidgetIndex(), entry);
        }

        public Collection<DroneDebugEntry> getAll() {
            return debugEntries.values();
        }

        public DroneDebugEntry get(int widgetId) {
            return debugEntries.get(widgetId);
        }

        public DroneDebugEntry getCurrent() {
            return debugEntries.get(DroneDebugger.this.getActiveWidgetIndex());
        }

    }
}
