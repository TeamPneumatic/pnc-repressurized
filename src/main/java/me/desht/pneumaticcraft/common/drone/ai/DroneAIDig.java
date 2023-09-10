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

import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.IToolUser;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.mixin.accessors.ServerPlayerGameModeAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class DroneAIDig<W extends ProgWidgetAreaItemBase & IToolUser> extends DroneAIBlockInteraction<W> {
    public DroneAIDig(IDroneBase drone, W widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        BlockState blockState = worldCache.getBlockState(pos);
        if (!ignoreBlock(blockState) && !pos.equals(drone.getControllerPos())) {
            List<ItemStack> drops = getDrops(worldCache, pos, drone);
            if (drops.isEmpty()) {
                // for those blocks which drop no items at all, try a by-block check
                return progWidget.isItemValidForFilters(ItemStack.EMPTY, blockState)
                        && (swapBestItemToFirstSlot(pos) || !progWidget.requiresTool());
            } else {
                // match by dropped items as normal
                for (ItemStack droppedStack : drops) {
                    if (progWidget.isItemValidForFilters(droppedStack, blockState)) {
                        return swapBestItemToFirstSlot(pos) || !progWidget.requiresTool();
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected boolean respectClaims() {
        return true;
    }

    /**
     * Equip the best tool in the drone's inventory to break the block at the given pos.
     * @param pos blockpos to break
     * @return true if the drone has any tool which is better than using an empty hand
     */
    private boolean swapBestItemToFirstSlot(BlockPos pos) {
        ItemStack currentStackSaved = drone.getInv().getStackInSlot(0).copy();

        // get relative hardness for empty hand
        drone.getInv().setStackInSlot(0, ItemStack.EMPTY);
        float baseSoftness = worldCache.getBlockState(pos).getDestroyProgress(drone.getFakePlayer(), drone.world(), pos);
        drone.getInv().setStackInSlot(0, currentStackSaved);
        boolean hasDiggingTool = false;

        // now find the best tool which is better than an empty hand
        int bestSlot = 0;
        float bestSoftness = Float.MIN_VALUE;
        BlockState state = worldCache.getBlockState(pos);
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            drone.getInv().setStackInSlot(0, drone.getInv().getStackInSlot(i));
            float softness = state.getDestroyProgress(drone.getFakePlayer(), drone.world(), pos);
            if (softness > bestSoftness) {
                bestSlot = i;
                bestSoftness = softness;

                if (softness > baseSoftness) {
                    hasDiggingTool = true;
                }
            }
        }
        drone.getInv().setStackInSlot(0, currentStackSaved);
        if (bestSlot != 0) {
            ItemStack bestItem = drone.getInv().getStackInSlot(bestSlot).copy();
            drone.getInv().setStackInSlot(bestSlot, drone.getInv().getStackInSlot(0));
            drone.getInv().setStackInSlot(0, bestItem);
        }
        return hasDiggingTool || !state.requiresCorrectToolForDrops();
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        ServerPlayerGameMode manager = drone.getFakePlayer().gameMode;
        ServerPlayerGameModeAccess access = (ServerPlayerGameModeAccess) manager;
        if (!access.isDestroyingBlock() || !access.hasDelayedDestroy()) { //is not destroying and is not acknowledged.
            BlockState blockState = worldCache.getBlockState(pos);
            if (!ignoreBlock(blockState) && isBlockValidForFilter(worldCache, pos, drone, progWidget)) {
                if (blockState.getDestroySpeed(drone.world(), pos) < 0) {
                    addToBlacklist(pos);
                    drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.dig.debug.cantDigBlock", pos);
                    drone.setDugBlock(null);
                    return false;
                }
                PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(drone.getFakePlayer(), pos, Direction.UP, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
                if (!event.isCanceled()) {
                    int limit = drone.world().getMaxBuildHeight();
                    manager.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, Direction.DOWN, limit, 0);
                    manager.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, Direction.DOWN, limit, 1);
                    drone.setDugBlock(pos);
                    return true;
                }
            }
            drone.setDugBlock(null);
            return false;
        } else {
            return true;
        }
    }

    public static boolean isBlockValidForFilter(BlockGetter worldCache, BlockPos pos, IDroneBase drone, ProgWidgetAreaItemBase widget) {
        BlockState blockState = worldCache.getBlockState(pos);

        if (!blockState.isAir()) {
            for (ItemStack droppedStack : getDrops(worldCache, pos, drone)) {
                if (widget.isItemValidForFilters(droppedStack, blockState)) {
                    return true;
                }
            }
            return widget.isItemValidForFilters(ItemStack.EMPTY, blockState);  // try a by-block check
        }
        return false;
    }

    private static List<ItemStack> getDrops(BlockGetter worldCache, BlockPos pos, IDroneBase drone) {
        BlockState state = worldCache.getBlockState(pos);
        DroneEntity d = drone instanceof DroneEntity ? (DroneEntity) drone : null;
        return state.getDrops(new LootParams.Builder((ServerLevel) drone.world())
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, drone.getInv().getStackInSlot(0))
                .withOptionalParameter(LootContextParams.THIS_ENTITY, d)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, worldCache.getBlockEntity(pos))
        );
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean ignoreBlock(BlockState state) {
        return state.isAir() || PneumaticCraftUtils.isBlockLiquid(state.getBlock());
    }
}
