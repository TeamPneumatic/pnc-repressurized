package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class DroneGoToChargingStation extends Goal {
    private final EntityDrone drone;
    public boolean isExecuting;
    private TileEntityChargingStation curCharger;
    private int chargingTime;

    public DroneGoToChargingStation(EntityDrone drone) {
        this.drone = drone;
        setMutexFlags(EnumSet.allOf(Flag.class)); // exclusive to all other AI tasks.
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        List<TileEntityChargingStation> validChargingStations = new ArrayList<>();
        drone.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY).ifPresent(h -> {
            if (h.getPressure() < PneumaticValues.DRONE_LOW_PRESSURE) {
                for (TileEntityChargingStation station : GlobalTileEntityCacheManager.getInstance().chargingStations) {
                    if (station.getWorld() == drone.world) {
                        BlockPos pos = new BlockPos(station.getPos().getX(), station.getPos().getY(), station.getPos().getZ());
                        if (DroneClaimManager.getInstance(drone.world).isClaimed(pos)) {
                            drone.addDebugEntry("pneumaticcraft.gui.progWidget.chargingStation.debug.claimed", pos);
                        } else if (station.getPressure() <= PneumaticValues.DRONE_LOW_PRESSURE) {
                            drone.addDebugEntry("pneumaticcraft.gui.progWidget.chargingStation.debug.notEnoughPressure", pos);
                        } else if (station.getUpgrades(EnumUpgrade.DISPENSER) == 0) {
                            drone.addDebugEntry("pneumaticcraft.gui.progWidget.chargingStation.debug.noDispenserUpgrades", pos);
                        } else {
                            validChargingStations.add(station);
                        }
                    }
                }
            }
        });

        validChargingStations.sort(Comparator.comparingDouble(arg -> PneumaticCraftUtils.distBetweenSq(arg.getPos().getX(), arg.getPos().getY(), arg.getPos().getZ(), drone.getPosX(), drone.getPosX(), drone.getPosX())));

        for (TileEntityChargingStation station : validChargingStations) {
            boolean protect = TileEntitySecurityStation.getProtectingSecurityStations(drone.getFakePlayer(), station.getPos(), false, false) > 0;
            BlockPos pos = new BlockPos(station.getPos().getX(), station.getPos().getY(), station.getPos().getZ());
            if (protect) {
                drone.addDebugEntry("pneumaticcraft.gui.progWidget.chargingStation.debug.protected", pos);
            } else if (drone.getPathNavigator().moveToXYZ(station.getPos().getX(), station.getPos().getY() + 1, station.getPos().getZ()) || drone.getPathNavigator().isGoingToTeleport()) {
                isExecuting = true;
                curCharger = station;
                DroneClaimManager.getInstance(drone.world).claim(pos);
                return true;
            } else {
                drone.addDebugEntry("pneumaticcraft.gui.progWidget.chargingStation.debug.cantNavigate", pos);
            }
        }
        isExecuting = false;
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting() {
        if (curCharger.getUpgrades(EnumUpgrade.DISPENSER) == 0 || curCharger.isRemoved()) {
            isExecuting = false;
            return false;
        } else if (!drone.getPathNavigator().isGoingToTeleport() && (drone.getNavigator().getPath() == null || drone.getNavigator().getPath().isFinished())) {
            isExecuting = drone.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
                    .map(h -> h.getPressure() < 9.9F && curCharger.getPressure() > h.getPressure() + 0.1F)
                    .orElseThrow(IllegalStateException::new);
            if (isExecuting) {
                chargingTime++;
                if (chargingTime > 20) {
                    drone.getPathNavigator().moveToXYZ(curCharger.getPos().getX(), curCharger.getPos().getY() + 1.5, curCharger.getPos().getZ());
                    if (drone.getNavigator().getPath() == null || drone.getNavigator().getPath().isFinished()) {
                        drone.setStandby(true);
                    } else {
                        chargingTime = 0;
                    }
                }
                DroneClaimManager.getInstance(drone.world).claim(new BlockPos(curCharger.getPos().getX(), curCharger.getPos().getY(), curCharger.getPos().getZ()));
            }
            return isExecuting;
        } else {
            chargingTime = 0;
            DroneClaimManager.getInstance(drone.world).claim(new BlockPos(curCharger.getPos().getX(), curCharger.getPos().getY(), curCharger.getPos().getZ()));
            return drone.isAccelerating();
        }
    }
}
