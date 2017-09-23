package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered.EnumOrder;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDigAndPlace;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetPlace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.ThreadedSorter;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class DroneAIBlockInteraction<Widget extends ProgWidgetAreaItemBase> extends EntityAIBase {
    protected final IDroneBase drone;
    protected final Widget widget;
    private final EnumOrder order;
    protected BlockPos curPos;
    private final List<BlockPos> area;
    protected final IBlockAccess worldCache;
    private final List<BlockPos> blacklist = new ArrayList<BlockPos>();//a list of position which weren't allowed to be digged in the past.
    private int curY;
    private int lastSuccessfulY;
    private int minY, maxY;
    private ThreadedSorter<BlockPos> sorter;
    private boolean aborted;

    protected boolean searching; //true while the drone is searching for a coordinate, false if traveling/processing a coordinate.
    private int searchIndex;//The current index in the area list the drone is searching at.
    private static final int LOOKUPS_PER_SEARCH_TICK = 30; //How many blocks does the drone access per AI update.
    private int totalActions;
    private int maxActions = -1;

    /**
     * @param drone
     * @param widget needs to implement IBlockOrdered
     */
    public DroneAIBlockInteraction(IDroneBase drone, Widget widget) {
        this.drone = drone;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
        this.widget = widget;
        order = widget instanceof IBlockOrdered ? ((IBlockOrdered) widget).getOrder() : EnumOrder.CLOSEST;
        area = widget.getCachedAreaList();
        worldCache = ProgWidgetAreaItemBase.getCache(area, drone.world());
        if (area.size() > 0) {
            Iterator<BlockPos> iterator = area.iterator();
            BlockPos pos = iterator.next();
            minY = maxY = pos.getY();
            while (iterator.hasNext()) {
                pos = iterator.next();
                minY = Math.min(minY, pos.getY());
                maxY = Math.max(maxY, pos.getY());
            }
            if (order == EnumOrder.HIGH_TO_LOW) {
                curY = maxY;
            } else if (order == EnumOrder.LOW_TO_HIGH) {
                curY = minY;
            }
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        if (aborted || maxActions >= 0 && totalActions >= maxActions) {
            return false;
        } else {
            if (!searching) {
                searching = true;
                searchIndex = 0;
                curPos = null;
                lastSuccessfulY = curY;
                if (sorter == null || sorter.isDone())
                    sorter = new ThreadedSorter(area, new ChunkPositionSorter(drone));
                return true;
            } else {
                return false;
            }
        }
    }

    private void updateY() {
        searchIndex = 0;
        if (order == ProgWidgetPlace.EnumOrder.LOW_TO_HIGH) {
            if (++curY > maxY) curY = minY;
        } else if (order == ProgWidgetPlace.EnumOrder.HIGH_TO_LOW) {
            if (--curY < minY) curY = maxY;
        }
    }

    private boolean isYValid(int y) {
        return order == ProgWidgetPlace.EnumOrder.CLOSEST || y == curY;
    }

    public DroneAIBlockInteraction setMaxActions(int maxActions) {
        this.maxActions = maxActions;
        return this;
    }

    protected abstract boolean isValidPosition(BlockPos pos);

    protected abstract boolean doBlockInteraction(BlockPos pos, double distToBlock);

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting() {
        if (aborted) return false;
        if (searching) {
            if (!sorter.isDone()) return true;//Wait until the area is sorted from closest to furtherest.
            boolean firstRun = true;
            int searchedBlocks = 0; //keeps track of the looked up blocks, and stops searching when we reach our quota.
            while (curPos == null && curY != lastSuccessfulY && order != ProgWidgetDigAndPlace.EnumOrder.CLOSEST || firstRun) {
                firstRun = false;
                while (!shouldAbort() && searchIndex < area.size()) {
                    BlockPos pos = area.get(searchIndex);
                    if (isYValid(pos.getY()) && !blacklist.contains(pos) && (!respectClaims() || !DroneClaimManager.getInstance(drone.world()).isClaimed(pos))) {
                        indicateToListeningPlayers(pos);
                        if (isValidPosition(pos)) {
                            curPos = pos;
                            if (moveToPositions()) {
                                if (moveIntoBlock()) {
                                    if (drone.getPathNavigator().moveToXYZ(curPos.getX(), curPos.getY() + 0.5, curPos.getZ())) {
                                        searching = false;
                                        totalActions++;
                                        if (respectClaims()) DroneClaimManager.getInstance(drone.world()).claim(pos);
                                        blacklist.clear();//clear the list for next time (maybe the blocks/rights have changed by the time there will be dug again).
                                        return true;
                                    }
                                } else {
                                    for (EnumFacing dir : EnumFacing.VALUES) {
                                        if (drone.getPathNavigator().moveToXYZ(curPos.getX() + dir.getFrontOffsetX(), curPos.getY() + dir.getFrontOffsetY() + 0.5, curPos.getZ() + dir.getFrontOffsetZ())) {
                                            searching = false;
                                            totalActions++;
                                            if (respectClaims())
                                                DroneClaimManager.getInstance(drone.world()).claim(pos);
                                            blacklist.clear();//clear the list for next time (maybe the blocks/rights have changed by the time there will be dug again).
                                            return true;
                                        }
                                    }
                                }
                                if (drone.getPathNavigator().isGoingToTeleport()) {
                                    searching = false;
                                    totalActions++;
                                    if (respectClaims()) DroneClaimManager.getInstance(drone.world()).claim(pos);
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
                    if (searchedBlocks >= lookupsPerSearch()) return true;
                }
                if (curPos == null) updateY();
            }
            if (!shouldAbort()) addEndingDebugEntry();
            return false;
        } else {
            Vec3d dronePos = drone.getDronePos();
            double dist = curPos != null ? PneumaticCraftUtils.distBetween(curPos.getX() + 0.5, curPos.getY() + 0.5, curPos.getZ() + 0.5, dronePos.x, dronePos.y, dronePos.z) : 0;
            if (curPos != null) {
                if (!moveToPositions()) return doBlockInteraction(curPos, dist);
                if (respectClaims()) DroneClaimManager.getInstance(drone.world()).claim(curPos);
                if (dist < (moveIntoBlock() ? 1 : 2)) {
                    return doBlockInteraction(curPos, dist);
                }
            }
            return !drone.getPathNavigator().hasNoPath();
        }
    }

    protected void addEndingDebugEntry() {
        drone.addDebugEntry("gui.progWidget.blockInteraction.debug.noBlocksValid");
    }

    protected int lookupsPerSearch() {
        return LOOKUPS_PER_SEARCH_TICK;
    }

    protected boolean respectClaims() {
        return false;
    }

    protected boolean moveIntoBlock() {
        return false;
    }

    protected boolean shouldAbort() {
        return aborted;
    }

    public void abort() {
        aborted = true;
    }

    protected boolean moveToPositions() {
        return true;
    }

    /**
     * Sends particle spawn packets to any close player that has a charged pneumatic helmet with entity tracker.
     *
     * @param pos
     */
    protected void indicateToListeningPlayers(BlockPos pos) {
        for (EntityPlayer player : drone.world().playerEntities) {
            ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (helmet.getItem() == Itemss.PNEUMATIC_HELMET && ItemPneumaticArmor.getUpgrades(EnumUpgrade.ENTITY_TRACKER, helmet) > 0
                    && ((IPressurizable) Itemss.PNEUMATIC_HELMET).getPressure(helmet) > 0) {
                NetworkHandler.sendTo(new PacketSpawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0), (EntityPlayerMP) player);
            }
        }
    }

    protected void addToBlacklist(BlockPos coord) {
        blacklist.add(coord);
        drone.sendWireframeToClient(coord);
    }

}
