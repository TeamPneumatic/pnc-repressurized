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

package me.desht.pneumaticcraft.common.debug;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.drone.debug.DroneDebugEntry;
import me.desht.pneumaticcraft.api.drone.debug.IDroneDebugger;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSendDroneDebugEntry;
import me.desht.pneumaticcraft.common.network.PacketSyncDroneProgWidgets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DroneDebugger implements IDroneDebugger {
    private final IDroneBase drone;
    private final DroneDebugList debugList = new DroneDebugList();
    private final Set<ServerPlayer> debuggingPlayers = new HashSet<>();  // players who receive debug data

    public DroneDebugger(IDroneBase drone) {
        this.drone = drone;
    }

    @Override
    public DroneDebugEntry getDebugEntry(int widgetID) {
        return debugList.get(widgetID);
    }

    @Override
    public void addEntry(String message) {
        addEntry(message, null);
    }

    @Override
    public void addEntry(String message, BlockPos pos) {
        DroneDebugEntry entry = DroneDebugEntry.create(message, pos, getActiveWidgetIndex());

        // add the entry server-side
        addEntry(entry);

        // add the entry client-side
        PacketSendDroneDebugEntry packet = PacketSendDroneDebugEntry.create(drone, entry);
        for (ServerPlayer player : debuggingPlayers) {
            NetworkHandler.sendToPlayer(packet, player);
        }
    }

    @Override
    public void addEntry(DroneDebugEntry entry) {
        debugList.addEntry(entry);
    }

    @Override
    public void trackAsDebugged(ServerPlayer player) {
        NetworkHandler.sendToPlayer(PacketSyncDroneProgWidgets.create(drone), player);

        for (DroneDebugEntry entry : debugList.getAll()) {
            NetworkHandler.sendToPlayer(PacketSendDroneDebugEntry.create(drone, entry), player);
        }

        debuggingPlayers.add(player);
    }

    public void updateDebuggingPlayers() {
        debuggingPlayers.removeIf(player -> !player.isAlive() || !PneumaticArmorItem.isPlayerDebuggingDrone(player, drone));
    }

    @Override
    public Collection<ServerPlayer> getDebuggingPlayers() {
        return debuggingPlayers;
    }

    private int getActiveWidgetIndex() {
        return drone.getActiveWidgetIndex();
    }

    private class DroneDebugList {
        private final Int2ObjectMap<DroneDebugEntry> debugEntries = new Int2ObjectOpenHashMap<>();

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
