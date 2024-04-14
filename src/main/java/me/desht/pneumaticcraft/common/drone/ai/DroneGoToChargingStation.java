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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.entity.utility.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.utility.SecurityStationBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.DroneClaimManager;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.GlobalBlockEntityCacheManager;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class DroneGoToChargingStation extends Goal {
    private final DroneEntity drone;
    public boolean isExecuting;
    private ChargingStationBlockEntity curCharger;
    private int chargingTime;

    public DroneGoToChargingStation(DroneEntity drone) {
        this.drone = drone;
        setFlags(EnumSet.allOf(Flag.class)); // exclusive to all other AI tasks.
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean canUse() {
        List<ChargingStationBlockEntity> validChargingStations = new ArrayList<>();
        IAirHandler handler = drone.getAirHandler();
        if (handler.getPressure() < PneumaticValues.DRONE_LOW_PRESSURE && handler.getPressure() > 0.001f) {
            int maxDistSq = ConfigHelper.common().drones.maxDroneChargingStationSearchRange.get()
                    * ConfigHelper.common().drones.maxDroneChargingStationSearchRange.get();
            for (ChargingStationBlockEntity station : GlobalBlockEntityCacheManager.getInstance(drone.level()).getChargingStations()) {
                Level level = drone.level();
                BlockPos chargingPos = station.getBlockPos();
                if (station.getLevel() == level && level.isLoaded(chargingPos) && drone.distanceToSqr(Vec3.atCenterOf(chargingPos)) <= maxDistSq) {
                    if (DroneClaimManager.getInstance(drone.level()).isClaimed(chargingPos)) {
                        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.chargingStation.debug.claimed", chargingPos);
                    } else if (station.getPressure() <= PneumaticValues.DRONE_LOW_PRESSURE) {
                        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.chargingStation.debug.notEnoughPressure", chargingPos);
                    } else if (station.getUpgrades(ModUpgrades.DISPENSER.get()) == 0) {
                        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.chargingStation.debug.noDispenserUpgrades", chargingPos);
                    } else {
                        validChargingStations.add(station);
                    }
                }
            }
        }

        validChargingStations.sort(Comparator.comparingDouble(te -> te.getBlockPos().distSqr(drone.blockPosition())));

        for (ChargingStationBlockEntity station : validChargingStations) {
            if (SecurityStationBlockEntity.isProtectedFromPlayer(drone.getFakePlayer(), station.getBlockPos(), false)) {
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.chargingStation.debug.protected", station.getBlockPos());
            } else if (drone.getPathNavigator().moveToXYZ(station.getBlockPos().getX() + 0.5, station.getBlockPos().getY() + 1, station.getBlockPos().getZ() + 0.5)
                    || drone.getPathNavigator().isGoingToTeleport()) {
                isExecuting = true;
                curCharger = station;
                DroneClaimManager.getInstance(drone.level()).claim(station.getBlockPos());
                return true;
            } else {
                drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.chargingStation.debug.cantNavigate", station.getBlockPos());
            }
        }
        isExecuting = false;
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean canContinueToUse() {
        if (curCharger.getUpgrades(ModUpgrades.DISPENSER.get()) == 0 || curCharger.isRemoved()) {
            isExecuting = false;
            return false;
        } else if (!drone.getPathNavigator().isGoingToTeleport() && (drone.getNavigation().getPath() == null || drone.getNavigation().getPath().isDone())) {
            IAirHandler handler = drone.getAirHandler();
            isExecuting = handler.getPressure() < 9.9F && curCharger.getPressure() > handler.getPressure() + 0.1F;
            if (isExecuting) {
                chargingTime++;
                if (chargingTime > 20) {
                    drone.getPathNavigator().moveToXYZ(curCharger.getBlockPos().getX() + 0.5, curCharger.getBlockPos().getY() + 1.5, curCharger.getBlockPos().getZ() + 0.5);
                    if (drone.getNavigation().getPath() == null || drone.getNavigation().getPath().isDone()) {
                        drone.setStandby(true, false);
                    } else {
                        chargingTime = 0;
                    }
                }
                DroneClaimManager.getInstance(drone.level()).claim(curCharger.getBlockPos());
            }
            return isExecuting;
        } else {
            chargingTime = 0;
            DroneClaimManager.getInstance(drone.level()).claim(curCharger.getBlockPos());
            return drone.isAccelerating();
        }
    }
}
