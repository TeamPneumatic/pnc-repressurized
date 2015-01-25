package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.IAreaProvider;
import pneumaticCraft.common.progwidgets.IGotoWidget;
import pneumaticCraft.common.progwidgets.ProgWidget;

public class DroneEntityAIGoToLocation extends EntityAIBase{
    protected final EntityDrone drone;
    private final double speed;
    private final ProgWidget gotoWidget;
    private final ChunkPositionSorter positionSorter;
    private final List<ChunkPosition> validArea;

    public DroneEntityAIGoToLocation(EntityDrone drone, double speed, ProgWidget gotoWidget){
        this.drone = drone;
        this.speed = speed;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
        this.gotoWidget = gotoWidget;
        validArea = new ArrayList<ChunkPosition>(((IAreaProvider)gotoWidget).getArea());
        positionSorter = new ChunkPositionSorter(drone);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        Collections.sort(validArea, positionSorter);
        for(ChunkPosition c : validArea) {
            if(drone.getDistanceSq(c.chunkPosX + 0.5, c.chunkPosY + 0.5, c.chunkPosZ + 0.5) < 0.50) return false;
            if(drone.getNavigator().tryMoveToXYZ(c.chunkPosX, c.chunkPosY, c.chunkPosZ, speed)) {
                return !((IGotoWidget)gotoWidget).doneWhenDeparting();
            }
        }
        return ((EntityPathNavigateDrone)drone.getNavigator()).isGoingToTeleport();

    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting(){
        return !drone.getNavigator().noPath();
    }
}
