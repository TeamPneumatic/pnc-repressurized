package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered.EnumOrder;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDigAndPlace;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetPlace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.ThreadedSorter;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public abstract class DroneAIBlockInteraction<W extends ProgWidgetAreaItemBase> extends Goal {
    protected final IDroneBase drone;
    protected final W progWidget;
    private final EnumOrder order;
    private BlockPos curPos;
    private final List<BlockPos> area;
    final IBlockReader worldCache;
    private final List<BlockPos> blacklist = new ArrayList<>();//a list of position which weren't allowed to be digged in the past.
    private int curY;
    private int lastSuccessfulY;
    private int minY, maxY;
    private ThreadedSorter<BlockPos> sorter;
    private boolean aborted;

    private boolean searching; //true while the drone is searching for a coordinate, false if traveling/processing a coordinate.
    private int searchIndex;//The current index in the area list the drone is searching at.
    private static final int LOOKUPS_PER_SEARCH_TICK = 30; //How many blocks does the drone access per AI update.
    private int totalActions;
    private int maxActions = -1;

    /**
     * @param drone the drone
     * @param progWidget needs to implement IBlockOrdered
     */
    public DroneAIBlockInteraction(IDroneBase drone, W progWidget) {
        this.drone = drone;
        setMutexFlags(EnumSet.allOf(Flag.class)); // exclusive to all other AI tasks
        this.progWidget = progWidget;
        order = progWidget instanceof IBlockOrdered ? ((IBlockOrdered) progWidget).getOrder() : EnumOrder.CLOSEST;
        area = progWidget.getCachedAreaList();
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
                    sorter = new ThreadedSorter<>(area, new ChunkPositionSorter(drone));
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

    public DroneAIBlockInteraction<W> setMaxActions(int maxActions) {
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
                                if (tryMoveToBlock(pos)) {
                                    return true;
                                }
                                if (drone.getPathNavigator().isGoingToTeleport()) {
                                    return movedToBlockOK(pos);
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

    private boolean tryMoveToBlock(BlockPos pos) {
        if (moveIntoBlock()) {
            if (drone.getPathNavigator().moveToXYZ(curPos.getX(), curPos.getY() + 0.5, curPos.getZ())) {
                return movedToBlockOK(pos);
            }
        } else {
            for (Direction dir : Direction.VALUES) {
                if (drone.getPathNavigator().moveToXYZ(curPos.getX() + dir.getXOffset(), curPos.getY() + dir.getYOffset() + 0.5, curPos.getZ() + dir.getZOffset())) {
                    return movedToBlockOK(pos);
                }
            }
        }
        return false;
    }

    private boolean movedToBlockOK(BlockPos pos) {
        searching = false;
        totalActions++;
        if (respectClaims()) DroneClaimManager.getInstance(drone.world()).claim(pos);
        blacklist.clear(); //clear the list for next time (maybe the blocks/rights have changed by the time there will be dug again).
        return true;
    }

    protected void addEndingDebugEntry() {
        drone.addDebugEntry("gui.progWidget.blockInteraction.debug.noBlocksValid");
    }

    private int lookupsPerSearch() {
        return LOOKUPS_PER_SEARCH_TICK;
    }

    protected boolean respectClaims() {
        return false;
    }

    protected boolean moveIntoBlock() {
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean shouldAbort() {
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
     * @param pos the blockpos to indicate
     */
    private void indicateToListeningPlayers(BlockPos pos) {
        for (PlayerEntity player : drone.world().getPlayers()) {
            if (player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < 1024) {
                ItemStack helmet = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
                if (helmet.getItem() == ModItems.PNEUMATIC_HELMET.get()) {
                    CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                    if (handler.isArmorReady(EquipmentSlotType.HEAD) && handler.isEntityTrackerEnabled()
                            && handler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.ENTITY_TRACKER) > 0
                            && handler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.DISPENSER) > 0) {
                        NetworkHandler.sendToPlayer(new PacketSpawnParticle(new RedstoneParticleData(1.0f, 1.0f, 0.5f, 1.0f), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0), (ServerPlayerEntity) player);
                    }
                }
            }
        }
    }

    void addToBlacklist(BlockPos coord) {
        blacklist.add(coord);
        drone.sendWireframeToClient(coord);
    }

}
