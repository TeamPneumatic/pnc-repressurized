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
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class DroneAIPlace<W extends ProgWidgetAreaItemBase /*& IBlockOrdered & ISidedWidget*/> extends DroneAIBlockInteraction<W> {
    /**
     * @param drone the drone
     * @param widget needs to implement IBlockOrdered as well as ProgWidgetAreaItemBase
     */
    public DroneAIPlace(IDroneBase drone, W widget) {
        super(drone, widget);
    }

    @Override
    protected boolean respectClaims() {
        return true;
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (drone.world().getBlockState(pos).canBeReplaced()) {
            if (Vec3.atCenterOf(pos).distanceToSqr(drone.getDronePos()) < 1.2) {
                // too close - placement could be blocked by the drone
                return false;
            }
            boolean failedOnPlacement = false;
            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                ItemStack droneStack = drone.getInv().getStackInSlot(i);
                if (droneStack.getItem() instanceof BlockItem && progWidget.isItemValidForFilters(droneStack)) {
                    BlockPos placerPos = findClearSide(pos);
                    if (placerPos == null) {
                        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.place.debug.noClearSides", pos);
                        failedOnPlacement = true;
                        break;
                    }
                    Block placingBlock = ((BlockItem) droneStack.getItem()).getBlock();
                    BlockState state = placingBlock.getStateForPlacement(getPlacementContext(placerPos, pos, droneStack));
                    if (state == null) {
                        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.place.debug.cantPlaceBlock", pos);
                        failedOnPlacement = true;
                    } else if (worldCache.isUnobstructed(null, state.getShape(drone.world(), pos))) {
                        if (state.canSurvive(drone.world(), pos)) {
                            return true;
                        } else {
                            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.place.debug.cantPlaceBlock", pos);
                            failedOnPlacement = true;
                        }
                    } else {
                        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.place.debug.entityInWay", pos);
                        failedOnPlacement = true;
                    }
                }
            }
            if (!failedOnPlacement) abort();
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        if (squareDistToBlock < 2 * 2) {
            for (int slot = 0; slot < drone.getInv().getSlots(); slot++) {
                ItemStack droneStack = drone.getInv().getStackInSlot(slot);
                if (droneStack.getItem() instanceof BlockItem blockItem
                        && progWidget.isItemValidForFilters(droneStack)
                        && worldCache.getBlockState(pos).canBeReplaced())
                {
                    BlockPlaceContext ctx = getPlacementContext(pos, pos, droneStack);
                    if (progWidget.getCachedAreaSet().contains(ctx.getClickedPos())) {
                        InteractionResult res = blockItem.place(ctx);
                        if (res.consumesAction()) {
                            drone.addAirToDrone(-PneumaticValues.DRONE_USAGE_PLACE);
                            if (slot == 0 && drone.getInv().getStackInSlot(slot).isEmpty()) {
                                // kludge to force update of visible held item
                                drone.getInv().setStackInSlot(slot, ItemStack.EMPTY);
                            }
                            return false;
                        }
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private BlockPos findClearSide(BlockPos pos) {
        for (Direction side : DirectionUtil.VALUES) {
            BlockPos pos2 = pos.relative(side);
            if (drone.world().getBlockState(pos.relative(side)).isPathfindable(drone.world(), pos2, PathComputationType.AIR)) {
                return pos2;
            }
        }
        return null;
    }

    private BlockPlaceContext getPlacementContext(BlockPos placerPos, BlockPos targetPos, ItemStack droneStack) {
        BlockHitResult brtr = drone.world().clip(new ClipContext(
                Vec3.atCenterOf(placerPos),
                Vec3.atCenterOf(targetPos),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                drone.getFakePlayer()
        ));
        return new BlockPlaceContext(new DroneBlockItemUseContext(drone.getFakePlayer(), droneStack, brtr));
    }

    private static class DroneBlockItemUseContext extends UseOnContext {
        protected DroneBlockItemUseContext(Player droneFakePlayer, ItemStack heldItem, BlockHitResult rayTraceResultIn) {
            super(droneFakePlayer.level(), droneFakePlayer, InteractionHand.MAIN_HAND, heldItem, rayTraceResultIn);
        }
    }
}
