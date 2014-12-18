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
    private final double speed;
    public boolean isExecuting;
    public TileEntityChargingStation curCharger;

    public DroneGoToChargingStation(EntityDrone drone, double par2){
        this.drone = drone;
        speed = par2;
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
                    if(!DroneClaimManager.getInstance(drone.worldObj).isClaimed(new ChunkPosition(station.xCoord, station.yCoord, station.zCoord)) && station.getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.DRONE_LOW_PRESSURE && station.getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0) {
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
            if(drone.getNavigator().tryMoveToXYZ(station.xCoord, station.yCoord + 1.5, station.zCoord, speed) || ((EntityPathNavigateDrone)drone.getNavigator()).isGoingToTeleport()) {
                isExecuting = true;
                curCharger = station;
                DroneClaimManager.getInstance(drone.worldObj).claim(new ChunkPosition(station.xCoord, station.yCoord, station.zCoord));
                return true;
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
        if(drone.getNavigator().getPath() == null || curCharger.getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) == 0 || curCharger.isInvalid()) {//If our path was blocked.
            isExecuting = false;
            return false;
        } else if(drone.getNavigator().getPath().isFinished()) {
            isExecuting = drone.getPressure(null) < 9.9F && curCharger.getPressure(ForgeDirection.UNKNOWN) > drone.getPressure(null) + 0.1F;
            if(isExecuting) DroneClaimManager.getInstance(drone.worldObj).claim(new ChunkPosition(curCharger.xCoord, curCharger.yCoord, curCharger.zCoord));
            return isExecuting;
        } else {
            DroneClaimManager.getInstance(drone.worldObj).claim(new ChunkPosition(curCharger.xCoord, curCharger.yCoord, curCharger.zCoord));
            return true;
        }
    }
}
