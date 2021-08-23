package me.desht.pneumaticcraft.common.debug;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSendDroneDebugEntry;
import me.desht.pneumaticcraft.common.network.PacketSyncDroneEntityProgWidgets;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

    public Collection<ServerPlayerEntity> getDebuggingPlayers() {
        return debuggingPlayers;
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

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        // with thanks to @Zorn_Taov for this code, slightly adapted for drone debugger integration by desht...
        @SubscribeEvent
        public static void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
            if (!PNCConfig.Common.General.droneDebuggerPathParticles
                    || !(event.getEntityLiving() instanceof EntityDrone)
                    || event.getEntityLiving().level.isClientSide) {
                return;
            }

            EntityDrone drone = (EntityDrone) event.getEntityLiving();
            if (drone.getDebugger().debuggingPlayers.isEmpty()) return;

            PathNavigator navi = drone.getNavigation();
            if (drone.level instanceof ServerWorld && drone.level.getGameTime() % 10 == 0) { // only generate every 0.5 seconds, to try and cut back on packet spam
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
                    if (pos != null && drone.getDronePos().distanceToSqr(Vector3d.atCenterOf(pos)) > 1) {
                        spawnParticle(drone.getDebugger().debuggingPlayers, ParticleTypes.HEART,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0,
                                0, 0, 0, 0);
                    }
                }
            }
        }

        private static <T extends IParticleData> void spawnParticle(Set<ServerPlayerEntity> players, T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
            SSpawnParticlePacket packet = new SSpawnParticlePacket(type, false, posX, posY, posZ, (float)xOffset, (float)yOffset, (float)zOffset, (float)speed, particleCount);
            players.forEach(player -> player.connection.send(packet));
        }
    }
}
