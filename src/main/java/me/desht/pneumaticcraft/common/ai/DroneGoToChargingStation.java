package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DroneGoToChargingStation extends EntityAIBase {
    private final EntityDrone drone;
    public boolean isExecuting;
    public TileEntityChargingStation curCharger;
    private int chargingTime;

    public DroneGoToChargingStation(EntityDrone drone) {
        this.drone = drone;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        List<TileEntityChargingStation> validChargingStations = new ArrayList<TileEntityChargingStation>();
        if (drone.getPressure(null) < PneumaticValues.DRONE_LOW_PRESSURE) {
            for (TileEntity te : drone.world.loadedTileEntityList) {
                if (te instanceof TileEntityChargingStation) {
                    TileEntityChargingStation station = (TileEntityChargingStation) te;
                    BlockPos pos = new BlockPos(station.getPos().getX(), station.getPos().getY(), station.getPos().getZ());
                    if (DroneClaimManager.getInstance(drone.world).isClaimed(pos)) {
                        drone.addDebugEntry("gui.progWidget.chargingStation.debug.claimed", pos);
                    } else if (station.getPressure() <= PneumaticValues.DRONE_LOW_PRESSURE) {
                        drone.addDebugEntry("gui.progWidget.chargingStation.debug.notEnoughPressure", pos);
                    } else if (station.getUpgrades(EnumUpgrade.DISPENSER) == 0) {
                        drone.addDebugEntry("gui.progWidget.chargingStation.debug.noDispenserUpgrades", pos);
                    } else {
                        validChargingStations.add(station);
                    }
                }
            }
        }

        validChargingStations.sort(Comparator.comparingDouble(arg -> PneumaticCraftUtils.distBetween(arg.getPos().getX(), arg.getPos().getY(), arg.getPos().getZ(), drone.posX, drone.posY, drone.posZ)));

        for (TileEntityChargingStation station : validChargingStations) {
            boolean protect = PneumaticCraftUtils.getProtectingSecurityStations(drone.world, station.getPos(), drone.getFakePlayer(), false, false) > 0;
            BlockPos pos = new BlockPos(station.getPos().getX(), station.getPos().getY(), station.getPos().getZ());
            if (protect) {
                drone.addDebugEntry("gui.progWidget.chargingStation.debug.protected", pos);
            } else if (drone.getPathNavigator().moveToXYZ(station.getPos().getX(), station.getPos().getY() + 1.5, station.getPos().getZ()) || drone.getPathNavigator().isGoingToTeleport()) {
                isExecuting = true;
                curCharger = station;
                DroneClaimManager.getInstance(drone.world).claim(pos);
                return true;
            } else {
                drone.addDebugEntry("gui.progWidget.chargingStation.debug.cantNavigate", pos);
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
        if (curCharger.getUpgrades(EnumUpgrade.DISPENSER) == 0 || curCharger.isInvalid()) {//If our path was blocked.
            isExecuting = false;
            return false;
        } else if (!drone.getPathNavigator().isGoingToTeleport() && (drone.getNavigator().getPath() == null || drone.getNavigator().getPath().isFinished())) {
            isExecuting = drone.getPressure(null) < 9.9F && curCharger.getPressure() > drone.getPressure(null) + 0.1F;
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
