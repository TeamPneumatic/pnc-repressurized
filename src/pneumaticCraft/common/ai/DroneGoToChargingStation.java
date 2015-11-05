package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;

public class DroneGoToChargingStation extends EntityAIBase{
    private final EntityDrone drone;
    public boolean isExecuting;
    public TileEntityChargingStation curCharger;
    private int chargingTime;

    public DroneGoToChargingStation(EntityDrone drone){
        this.drone = drone;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        List<TileEntityChargingStation> validChargingStations = new ArrayList<TileEntityChargingStation>();
        if(drone.getPressure(null) < PneumaticValues.DRONE_LOW_PRESSURE) {
            for(TileEntity te : (List<TileEntity>)drone.worldObj.loadedTileEntityList) {
                if(te instanceof TileEntityChargingStation) {
                    TileEntityChargingStation station = (TileEntityChargingStation)te;
                    ChunkPosition pos = new ChunkPosition(station.xCoord, station.yCoord, station.zCoord);
                    if(DroneClaimManager.getInstance(drone.worldObj).isClaimed(pos)) {
                        drone.addDebugEntry("gui.progWidget.chargingStation.debug.claimed", pos);
                    } else if(station.getPressure(ForgeDirection.UNKNOWN) <= PneumaticValues.DRONE_LOW_PRESSURE) {
                        drone.addDebugEntry("gui.progWidget.chargingStation.debug.notEnoughPressure", pos);
                    } else if(station.getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) == 0) {
                        drone.addDebugEntry("gui.progWidget.chargingStation.debug.noDispenserUpgrades", pos);
                    } else {
                        validChargingStations.add(station);
                    }
                }
            }
        }

        Collections.sort(validChargingStations, new Comparator(){
            @Override
            public int compare(Object arg1, Object arg2){
                TileEntity te1 = (TileEntity)arg1;
                TileEntity te2 = (TileEntity)arg2;
                return Double.compare(PneumaticCraftUtils.distBetween(te1.xCoord, te1.yCoord, te1.zCoord, drone.posX, drone.posY, drone.posZ), PneumaticCraftUtils.distBetween(te2.xCoord, te2.yCoord, te2.zCoord, drone.posX, drone.posY, drone.posZ));
            }
        });

        for(TileEntityChargingStation station : validChargingStations) {
            boolean protect = PneumaticCraftUtils.getProtectingSecurityStations(drone.worldObj, station.xCoord, station.yCoord, station.zCoord, drone.getFakePlayer(), false, false) > 0;
            ChunkPosition pos = new ChunkPosition(station.xCoord, station.yCoord, station.zCoord);
            if(protect) {
                drone.addDebugEntry("gui.progWidget.chargingStation.debug.protected", pos);
            } else if(drone.getPathNavigator().moveToXYZ(station.xCoord, station.yCoord + 1.5, station.zCoord) || drone.getPathNavigator().isGoingToTeleport()) {
                isExecuting = true;
                curCharger = station;
                DroneClaimManager.getInstance(drone.worldObj).claim(pos);
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
    public boolean continueExecuting(){
        if(curCharger.getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) == 0 || curCharger.isInvalid()) {//If our path was blocked.
            isExecuting = false;
            return false;
        } else if(!drone.getPathNavigator().isGoingToTeleport() && (drone.getNavigator().getPath() == null || drone.getNavigator().getPath().isFinished())) {
            isExecuting = drone.getPressure(null) < 9.9F && curCharger.getPressure(ForgeDirection.UNKNOWN) > drone.getPressure(null) + 0.1F;
            if(isExecuting) {
                chargingTime++;
                if(chargingTime > 20) {
                    drone.getPathNavigator().moveToXYZ(curCharger.xCoord, curCharger.yCoord + 1.5, curCharger.zCoord);
                    if(drone.getNavigator().getPath() == null || drone.getNavigator().getPath().isFinished()) {
                        drone.setStandby(true);
                    } else {
                        chargingTime = 0;
                    }
                }
                DroneClaimManager.getInstance(drone.worldObj).claim(new ChunkPosition(curCharger.xCoord, curCharger.yCoord, curCharger.zCoord));
            }
            return isExecuting;
        } else {
            chargingTime = 0;
            DroneClaimManager.getInstance(drone.worldObj).claim(new ChunkPosition(curCharger.xCoord, curCharger.yCoord, curCharger.zCoord));
            return drone.isAccelerating();
        }
    }
}
