package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.IBlockOrdered;
import pneumaticCraft.common.progwidgets.IBlockOrdered.EnumOrder;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.progwidgets.ProgWidgetDigAndPlace;
import pneumaticCraft.common.progwidgets.ProgWidgetPlace;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public abstract class DroneAIBlockInteraction extends EntityAIBase{
    protected final EntityDrone drone;
    private final double speed;
    protected final ProgWidgetAreaItemBase widget;
    private final EnumOrder order;
    private ChunkPosition curPos;
    private final Set<ChunkPosition> area;
    private final ChunkPositionSorter sorter;
    protected final IBlockAccess worldCache;
    private final List<ChunkPosition> blacklist = new ArrayList<ChunkPosition>();//a list of position which weren't allowed to be digged in the past.
    private int curY;
    private int minY, maxY;

    /**
     * 
     * @param drone
     * @param speed
     * @param widget needs to implement IBlockOrdered
     */
    public DroneAIBlockInteraction(EntityDrone drone, double speed, ProgWidgetAreaItemBase widget){
        this.drone = drone;
        this.speed = speed;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
        this.widget = widget;
        order = ((IBlockOrdered)widget).getOrder();
        area = widget.getArea();
        worldCache = ProgWidgetAreaItemBase.getCache(area, drone.worldObj);
        sorter = new ChunkPositionSorter(drone);
        if(area.size() > 0) {
            Iterator<ChunkPosition> iterator = area.iterator();
            ChunkPosition pos = iterator.next();
            minY = maxY = pos.chunkPosY;
            while(iterator.hasNext()) {
                pos = iterator.next();
                minY = Math.min(minY, pos.chunkPosY);
                maxY = Math.max(maxY, pos.chunkPosY);
            }
            if(order == EnumOrder.HIGH_TO_LOW) {
                curY = maxY;
            } else if(order == EnumOrder.LOW_TO_HIGH) {
                curY = minY;
            }
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        ChunkPosition bestPos = null;
        int startY = curY;
        boolean firstRun = true;
        while(bestPos == null && (curY != startY && order != ProgWidgetDigAndPlace.EnumOrder.CLOSEST || firstRun)) {
            firstRun = false;
            for(ChunkPosition pos : area) {
                if(isYValid(pos.chunkPosY) && !blacklist.contains(pos)) {
                    if(bestPos == null || sorter.compare(bestPos, pos) > 0) {
                        if(isValidPosition(pos)) {
                            bestPos = pos;
                        }
                    }
                }
            }
            if(bestPos == null) updateY();
        }
        if(bestPos != null) {
            for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if(drone.getNavigator().tryMoveToXYZ(bestPos.chunkPosX + dir.offsetX, bestPos.chunkPosY + dir.offsetY + 0.5, bestPos.chunkPosZ + dir.offsetZ, speed)) {
                    curPos = bestPos;
                    return true;
                }
            }
            if(((EntityPathNavigateDrone)drone.getNavigator()).isGoingToTeleport()) {
                curPos = bestPos;
                return true;
            }
        }
        blacklist.clear();//clear the list for next time (maybe the blocks/rights have changed by the time there will be dug again).
        return false;
    }

    private void updateY(){
        if(order == ProgWidgetPlace.EnumOrder.LOW_TO_HIGH) {
            if(++curY > maxY) curY = minY;
        } else if(order == ProgWidgetPlace.EnumOrder.HIGH_TO_LOW) {
            if(--curY < minY) curY = maxY;
        }
    }

    private boolean isYValid(int y){
        return order == ProgWidgetPlace.EnumOrder.CLOSEST || y == curY;
    }

    protected abstract boolean isValidPosition(ChunkPosition pos);

    protected abstract boolean doBlockInteraction(ChunkPosition pos, double distToBlock);

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting(){
        double dist = curPos != null ? PneumaticCraftUtils.distBetween(curPos.chunkPosX + 0.5, curPos.chunkPosY + 0.5, curPos.chunkPosZ + 0.5, drone.posX, drone.posY, drone.posZ) : 0;
        if(curPos != null && dist < 2) {
            return doBlockInteraction(curPos, dist);
        }
        return !drone.getNavigator().noPath();
    }

    protected void addToBlacklist(ChunkPosition coord){
        blacklist.add(coord);
        drone.sendWireframeToClient(coord.chunkPosX, coord.chunkPosY, coord.chunkPosZ);
    }

}
