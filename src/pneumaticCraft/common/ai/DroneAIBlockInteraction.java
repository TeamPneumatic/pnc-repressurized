package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSpawnParticle;
import pneumaticCraft.common.progwidgets.IBlockOrdered;
import pneumaticCraft.common.progwidgets.IBlockOrdered.EnumOrder;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.progwidgets.ProgWidgetDigAndPlace;
import pneumaticCraft.common.progwidgets.ProgWidgetPlace;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.common.util.ThreadedSorter;

public abstract class DroneAIBlockInteraction<Widget extends ProgWidgetAreaItemBase> extends EntityAIBase{
    protected final IDroneBase drone;
    protected final Widget widget;
    private final EnumOrder order;
    protected ChunkPosition curPos;
    private final List<ChunkPosition> area;
    protected final IBlockAccess worldCache;
    private final List<ChunkPosition> blacklist = new ArrayList<ChunkPosition>();//a list of position which weren't allowed to be digged in the past.
    private int curY;
    private int lastSuccessfulY;
    private int minY, maxY;
    private ThreadedSorter<ChunkPosition> sorter;
    private boolean aborted;

    protected boolean searching; //true while the drone is searching for a coordinate, false if traveling/processing a coordinate.
    private int searchIndex;//The current index in the area list the drone is searching at.
    private static final int LOOKUPS_PER_SEARCH_TICK = 30; //How many blocks does the drone access per AI update.
    private int totalActions;
    private int maxActions = -1;

    /**
     * 
     * @param drone
     * @param speed
     * @param widget needs to implement IBlockOrdered
     */
    public DroneAIBlockInteraction(IDroneBase drone, Widget widget){
        this.drone = drone;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
        this.widget = widget;
        order = widget instanceof IBlockOrdered ? ((IBlockOrdered)widget).getOrder() : EnumOrder.CLOSEST;
        area = widget.getCachedAreaList();
        worldCache = ProgWidgetAreaItemBase.getCache(area, drone.getWorld());
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
        if(aborted || maxActions >= 0 && totalActions >= maxActions) {
            return false;
        } else {
            if(!searching) {
                searching = true;
                searchIndex = 0;
                curPos = null;
                lastSuccessfulY = curY;
                if(sorter == null || sorter.isDone()) sorter = new ThreadedSorter(area, new ChunkPositionSorter(drone));
                return true;
            } else {
                return false;
            }
        }
    }

    private void updateY(){
        searchIndex = 0;
        if(order == ProgWidgetPlace.EnumOrder.LOW_TO_HIGH) {
            if(++curY > maxY) curY = minY;
        } else if(order == ProgWidgetPlace.EnumOrder.HIGH_TO_LOW) {
            if(--curY < minY) curY = maxY;
        }
    }

    private boolean isYValid(int y){
        return order == ProgWidgetPlace.EnumOrder.CLOSEST || y == curY;
    }

    public DroneAIBlockInteraction setMaxActions(int maxActions){
        this.maxActions = maxActions;
        return this;
    }

    protected abstract boolean isValidPosition(ChunkPosition pos);

    protected abstract boolean doBlockInteraction(ChunkPosition pos, double distToBlock);

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting(){
        if(aborted) return false;
        if(searching) {
            if(!sorter.isDone()) return true;//Wait until the area is sorted from closest to furtherest.
            boolean firstRun = true;
            int searchedBlocks = 0; //keeps track of the looked up blocks, and stops searching when we reach our quota.
            while(curPos == null && curY != lastSuccessfulY && order != ProgWidgetDigAndPlace.EnumOrder.CLOSEST || firstRun) {
                firstRun = false;
                while(!shouldAbort() && searchIndex < area.size()) {
                    ChunkPosition pos = area.get(searchIndex);
                    if(isYValid(pos.chunkPosY) && !blacklist.contains(pos) && (!respectClaims() || !DroneClaimManager.getInstance(drone.getWorld()).isClaimed(pos))) {
                        indicateToListeningPlayers(pos);
                        if(isValidPosition(pos)) {
                            curPos = pos;
                            if(moveToPositions()) {
                                if(moveIntoBlock()) {
                                    if(drone.getPathNavigator().moveToXYZ(curPos.chunkPosX, curPos.chunkPosY + 0.5, curPos.chunkPosZ)) {
                                        searching = false;
                                        totalActions++;
                                        if(respectClaims()) DroneClaimManager.getInstance(drone.getWorld()).claim(pos);
                                        blacklist.clear();//clear the list for next time (maybe the blocks/rights have changed by the time there will be dug again).
                                        return true;
                                    }
                                } else {
                                    for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                                        if(drone.getPathNavigator().moveToXYZ(curPos.chunkPosX + dir.offsetX, curPos.chunkPosY + dir.offsetY + 0.5, curPos.chunkPosZ + dir.offsetZ)) {
                                            searching = false;
                                            totalActions++;
                                            if(respectClaims()) DroneClaimManager.getInstance(drone.getWorld()).claim(pos);
                                            blacklist.clear();//clear the list for next time (maybe the blocks/rights have changed by the time there will be dug again).
                                            return true;
                                        }
                                    }
                                }
                                if(drone.getPathNavigator().isGoingToTeleport()) {
                                    searching = false;
                                    totalActions++;
                                    if(respectClaims()) DroneClaimManager.getInstance(drone.getWorld()).claim(pos);
                                    blacklist.clear();//clear the list for next time (maybe the blocks/rights have changed by the time there will be dug again).
                                    return true;
                                } else {
                                    drone.addDebugEntry("gui.progWidget.general.debug.cantNavigate", pos);
                                }
                            } else {
                                searching = false;
                                totalActions++;
                                return true;
                            }
                        }
                        searchedBlocks++;
                    }
                    searchIndex++;
                    if(searchedBlocks >= lookupsPerSearch()) return true;
                }
                if(curPos == null) updateY();
            }
            if(!shouldAbort()) addEndingDebugEntry();
            return false;
        } else {
            Vec3 dronePos = drone.getPosition();
            double dist = curPos != null ? PneumaticCraftUtils.distBetween(curPos.chunkPosX + 0.5, curPos.chunkPosY + 0.5, curPos.chunkPosZ + 0.5, dronePos.xCoord, dronePos.yCoord, dronePos.zCoord) : 0;
            if(curPos != null) {
                if(!moveToPositions()) return doBlockInteraction(curPos, dist);
                if(respectClaims()) DroneClaimManager.getInstance(drone.getWorld()).claim(curPos);
                if(dist < (moveIntoBlock() ? 1 : 2)) {
                    return doBlockInteraction(curPos, dist);
                }
            }
            return !drone.getPathNavigator().hasNoPath();
        }
    }

    protected void addEndingDebugEntry(){
        drone.addDebugEntry("gui.progWidget.blockInteraction.debug.noBlocksValid");
    }

    protected int lookupsPerSearch(){
        return LOOKUPS_PER_SEARCH_TICK;
    }

    protected boolean respectClaims(){
        return false;
    }

    protected boolean moveIntoBlock(){
        return false;
    }

    protected boolean shouldAbort(){
        return aborted;
    }

    public void abort(){
        aborted = true;
    }

    protected boolean moveToPositions(){
        return true;
    }

    /**
     * Sends particle spawn packets to any close player that has a charged pneumatic helmet with entity tracker.
     * @param pos
     */
    protected void indicateToListeningPlayers(ChunkPosition pos){
        for(EntityPlayer player : (List<EntityPlayer>)drone.getWorld().playerEntities) {
            if(player.getCurrentArmor(3) != null && player.getCurrentArmor(3).getItem() == Itemss.pneumaticHelmet && ItemPneumaticArmor.getUpgrades(ItemMachineUpgrade.UPGRADE_ENTITY_TRACKER, player.getCurrentArmor(3)) > 0 && ((IPressurizable)Itemss.pneumaticHelmet).getPressure(player.getCurrentArmor(3)) > 0) {
                NetworkHandler.sendTo(new PacketSpawnParticle("reddust", pos.chunkPosX + 0.5, pos.chunkPosY + 0.5, pos.chunkPosZ + 0.5, 0, 0, 0), (EntityPlayerMP)player);
            }
        }
    }

    protected void addToBlacklist(ChunkPosition coord){
        blacklist.add(coord);
        drone.sendWireframeToClient(coord.chunkPosX, coord.chunkPosY, coord.chunkPosZ);
    }

}
