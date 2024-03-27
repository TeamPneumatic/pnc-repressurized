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
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSendDroneDebugEntry;
import me.desht.pneumaticcraft.common.network.PacketSyncDroneProgWidgets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DroneDebugger {
    private final IDroneBase drone;
    private final DroneDebugList debugList = new DroneDebugList();
    private final Set<ServerPlayer> debuggingPlayers = new HashSet<>();  // players who receive debug data

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
        PacketSendDroneDebugEntry packet = PacketSendDroneDebugEntry.create(drone, entry);
        for (ServerPlayer player : debuggingPlayers) {
            NetworkHandler.sendToPlayer(packet, player);
        }
    }

    public void addEntry(DroneDebugEntry entry) {
        debugList.addEntry(entry);
    }

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

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        // with thanks to @Zorn_Taov for this code, slightly adapted for drone debugger integration by desht...
        @SubscribeEvent
        public static void onLivingUpdateEvent(LivingEvent.LivingTickEvent event) {
            if (!ConfigHelper.common().drones.droneDebuggerPathParticles.get()
                    || !(event.getEntity() instanceof DroneEntity drone)
                    || event.getEntity().level().isClientSide) {
                return;
            }

            if (drone.getDebugger().debuggingPlayers.isEmpty()) return;

            PathNavigation navi = drone.getNavigation();
            if (drone.level() instanceof ServerLevel && drone.level().getGameTime() % 10 == 0) { // only generate every 0.5 seconds, to try and cut back on packet spam
                Path path = navi.getPath();
                if (path != null) {
                    for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
                        //get current point
                        BlockPos pos = path.getNode(i).asBlockPos();  // asBlockPos() = copy()
                        //get next point (or current point)
                        BlockPos nextPos = (i+1) != path.getNodeCount() ? path.getNode(i+1).asBlockPos() : pos;
                        //get difference for vector
                        BlockPos endPos = nextPos.subtract(pos);
                        spawnParticle(drone.getDebugger().debuggingPlayers, ParticleTypes.HAPPY_VILLAGER,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0,
                                0, 0, 0, 0);
                        //send a particle between points for direction
                        spawnParticle(drone.getDebugger().debuggingPlayers, ParticleTypes.END_ROD,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0,
                                endPos.getX(), endPos.getY(), endPos.getZ(), 0.1);
                    }
                    // render end point
                    BlockPos pos = navi.getTargetPos();  // yes, this *can* be null: https://github.com/TeamPneumatic/pnc-repressurized/issues/761
                    //noinspection ConstantConditions
                    if (pos != null && drone.getDronePos().distanceToSqr(Vec3.atCenterOf(pos)) > 1) {
                        spawnParticle(drone.getDebugger().debuggingPlayers, ParticleTypes.HEART,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0,
                                0, 0, 0, 0);
                    }
                }
            }
        }

        private static <T extends ParticleOptions> void spawnParticle(Set<ServerPlayer> players, T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
            ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(type, false, posX, posY, posZ, (float)xOffset, (float)yOffset, (float)zOffset, (float)speed, particleCount);
            players.forEach(player -> player.connection.send(packet));
        }
    }
}
