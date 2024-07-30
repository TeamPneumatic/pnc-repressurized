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

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.DroneClaimManager;
import me.desht.pneumaticcraft.common.drone.progwidgets.IBlockOrdered;
import me.desht.pneumaticcraft.common.drone.progwidgets.IBlockOrdered.Ordering;
import me.desht.pneumaticcraft.common.drone.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnIndicatorParticles;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.ThreadedSorter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public abstract class DroneAIBlockInteraction<W extends ProgWidgetAreaItemBase> extends Goal {
    private static final int MAX_LOOKUPS_PER_SEARCH = 30;
    private static final int DRONE_DEBUG_PARTICLE_RANGE_SQ = 32 * 32;

    protected final IDrone drone;
    protected final W progWidget;
    private final Ordering order;
    private BlockPos curPos;
    private final List<BlockPos> area;
    final CollisionGetter worldCache;
    private final List<BlockPos> blacklist = new ArrayList<>(); //a list of position which weren't allowed to be dug in the past.
    private int curY;
    private int lastSuccessfulY;
    private int minY, maxY;
    private ThreadedSorter<BlockPos> sorter;
    private boolean aborted;
    private boolean searching; // True while the drone is searching for a coordinate, false if traveling to or processing a coordinate.
    private int searchIndex;   // The current index in the area list the drone is searching at.
    private int totalActions;
    private int maxActions = -1;

    /**
     * @param drone the drone
     * @param progWidget needs to implement IBlockOrdered
     */
    public DroneAIBlockInteraction(IDrone drone, W progWidget) {
        this.drone = drone;
        setFlags(EnumSet.allOf(Flag.class)); // exclusive to all other AI tasks
        this.progWidget = progWidget;
        order = progWidget instanceof IBlockOrdered ? ((IBlockOrdered) progWidget).getOrder() : Ordering.CLOSEST;
        area = progWidget.getCachedAreaList();
        worldCache = progWidget.getChunkCache(drone.getDroneLevel());

        if (!area.isEmpty()) {
            BoundingBox extents = progWidget.getAreaExtents();
            minY = extents.minY();
            maxY = extents.maxY();
            if (order == Ordering.HIGH_TO_LOW) {
                curY = maxY;
            } else if (order == Ordering.LOW_TO_HIGH) {
                curY = minY;
            }
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean canUse() {
        if (aborted || maxActions >= 0 && totalActions >= maxActions) {
            return false;
        } else {
            if (!searching) {
                searching = true;
                lastSuccessfulY = curY;
                curPos = null;
                searchIndex = 0;
                if (sorter == null || sorter.isDone()) {
                    sorter = new ThreadedSorter<>(area, new ChunkPositionSorter(drone, order));
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private void updateY() {
        searchIndex = 0;
        if (order == Ordering.LOW_TO_HIGH) {
            if (++curY > maxY) curY = minY;
        } else if (order == Ordering.HIGH_TO_LOW) {
            if (--curY < minY) curY = maxY;
        }
    }

    private boolean isYValid(int y) {
        return y < drone.getDroneLevel().getMaxBuildHeight() && y >= drone.getDroneLevel().getMinBuildHeight() && order == Ordering.CLOSEST || y == curY;
    }

    public DroneAIBlockInteraction<?> setMaxActions(int maxActions) {
        this.maxActions = maxActions;
        return this;
    }

    /**
     * Check if the given blockpos is valid for the purposes of this operation; generally the operation that is
     * carried out by {@link #doBlockInteraction(BlockPos, double)} should be attempted as a simulation.  If this
     * method returns true, then the drone will attempt to move there (if appropriate) and call
     * {@link #doBlockInteraction(BlockPos, double)} to actually carry out the operation.
     *
     * @param pos the block pos to examine
     * @return true if this pos is valid, false otherwise
     */
    protected abstract boolean isValidPosition(BlockPos pos);

    /**
     * Carry out the actual interaction operation.  Beware the return value: returning false is usually the right
     * thing to do when the operation succeeded, to indicate to the drone that it shouldn't keep trying this, but
     * instead move on to the next progwidget.
     *
     * @param pos the block pos to work on
     * @param squareDistToBlock squared distance from the drone to the block
     * @return false if we're done and should stop trying, true to try again next time
     */
    protected abstract boolean doBlockInteraction(BlockPos pos, double squareDistToBlock);

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean canContinueToUse() {
        if (aborted) return false;
        if (searching) {
            if (!sorter.isDone()) return true; // wait until the area is sorted according to the given ordering

            boolean firstRun = true;
            int searchedBlocks = 0; // tracks the number of inspected blocks; stop searching when MAX_LOOKUPS_PER_SEARCH is reached
            while (curPos == null && curY != lastSuccessfulY && order != Ordering.CLOSEST || firstRun) {
                firstRun = false;
                List<BlockPos> inspectedPositions = new ArrayList<>();
                while (!shouldAbort() && searchIndex < area.size()) {
                    BlockPos pos = area.get(searchIndex);
                    searchIndex++;
                    if (isYValid(pos.getY()) && !blacklist.contains(pos) && (!respectClaims() || !DroneClaimManager.getInstance(drone.getDroneLevel()).isClaimed(pos))) {
                        if (!drone.getDebugger().getDebuggingPlayers().isEmpty()) inspectedPositions.add(pos);
                        if (isValidPosition(pos)) {
                            curPos = pos;
                            if (moveToPositions()) {
                                if (tryMoveToBlock(pos)) {
                                    return true;
                                }
                                if (drone.getPathNavigator().isGoingToTeleport()) {
                                    return movedToBlockOK(pos);
                                } else {
                                    drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.general.debug.cantNavigate", pos);
                                }
                            } else {
                                searching = false;
                                totalActions++;
                                return true;
                            }
                        }
                        searchedBlocks++;
                    }
                    if (searchedBlocks >= MAX_LOOKUPS_PER_SEARCH) {
                        indicateToListeningPlayers(inspectedPositions);
                        return true;
                    }
                }
                indicateToListeningPlayers(inspectedPositions);
                if (curPos == null) updateY();
            }
            if (!shouldAbort()) addEndingDebugEntry();
            return false;
        } else {
            // found a block to interact with; we're now either moving to it, or we've arrived there
            // curPos *should* always be non-null here, but just to be defensive...
            if (curPos != null) {
                if (respectClaims()) {
                    DroneClaimManager.getInstance(drone.getDroneLevel()).claim(curPos);
                }
                double distSq = drone.getDronePos().distanceToSqr(Vec3.atCenterOf(curPos));
                if (!moveToPositions() || distSq < (moveIntoBlock() ? 1 : 4)) {  // 1 or 2 blocks
                    return doBlockInteraction(curPos, distSq);
                }
            }
            // if we end up here, we're either still travelling (return true) or have nowhere to go (return false)
            return !drone.getPathNavigator().hasNoPath();
        }
    }

    private boolean tryMoveToBlock(BlockPos pos) {
        if (moveIntoBlock()) {
            if (blockAllowsMovement(worldCache, curPos, worldCache.getBlockState(pos))
                    && drone.getPathNavigator().moveToXYZ(curPos.getX(), curPos.getY() + 0.5, curPos.getZ())) {
                return movedToBlockOK(pos);
            }
        } else {
            ISidedWidget w = progWidget instanceof ISidedWidget ? (ISidedWidget) progWidget : null;
            for (Direction dir : DirectionUtil.VALUES) {
                BlockPos pos2 = curPos.relative(dir);
                if (drone.getDronePos().distanceToSqr(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5) < 0.5) {
                    // consider that close enough already
                    return movedToBlockOK(pos);
                }
                if ((w == null || w.isSideSelected(dir))
                        && blockAllowsMovement(worldCache, pos2, worldCache.getBlockState(pos2))
                        && drone.getPathNavigator().moveToXYZ(pos2.getX(), pos2.getY() + 0.5, pos2.getZ())) {
                    return movedToBlockOK(pos);
                }
            }
        }
        return false;
    }

    private boolean blockAllowsMovement(CollisionGetter world, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof LiquidBlock liq ?
                drone.canMoveIntoFluid(liq.fluid) :
                world.getBlockState(pos).isPathfindable(PathComputationType.AIR);
    }

    private boolean movedToBlockOK(BlockPos pos) {
        searching = false;
        totalActions++;
        if (respectClaims()) DroneClaimManager.getInstance(drone.getDroneLevel()).claim(pos);
        blacklist.clear(); //clear the list for next time (maybe the blocks/rights have changed by the time there will be dug again).
        return true;
    }

    protected void addEndingDebugEntry() {
        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.blockInteraction.debug.noBlocksValid");
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
     * Sends particle spawn packets to any close player that has a charged pneumatic helmet with entity tracker enabled
     * and dispenser upgrade installed.
     *
     * @param pos the blockpos to indicate
     */
    private void indicateToListeningPlayers(List<BlockPos> pos) {
        if (!pos.isEmpty()) {
            for (ServerPlayer player : drone.getDebugger().getDebuggingPlayers()) {
                if (player.distanceToSqr(pos.getFirst().getX(), pos.getFirst().getY(), pos.getFirst().getZ()) < DRONE_DEBUG_PARTICLE_RANGE_SQ) {
                    CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                    if (handler.upgradeUsable(CommonUpgradeHandlers.entityTrackerHandler, true)
                            && handler.getUpgradeCount(EquipmentSlot.HEAD, ModUpgrades.DISPENSER.get()) > 0) {
                        NetworkHandler.sendToPlayer(PacketSpawnIndicatorParticles.create(pos, progWidget.getColor()), player);
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
