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
import me.desht.pneumaticcraft.common.drone.progwidgets.IBlockRightClicker;
import me.desht.pneumaticcraft.common.drone.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class DroneAIRightClickBlock extends DroneAIBlockInteraction<ProgWidgetAreaItemBase> {
    private final List<BlockPos> visitedPositions = new ArrayList<>();
    private final IBlockRightClicker.RightClickType clickType;

    public DroneAIRightClickBlock(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);

        if (widget instanceof IBlockRightClicker rc) {
            drone.getFakePlayer().setShiftKeyDown(rc.isSneaking());
            clickType = rc.getClickType();
        } else {
            throw new IllegalArgumentException("expecting a widget implementing IBlockRightClicker!");
        }
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (visitedPositions.contains(pos)) return false;
        if (progWidget.isItemFilterEmpty()) return true;
        switch (clickType) {
            case CLICK_ITEM -> {
                for (int i = 0; i < drone.getInv().getSlots(); i++) {
                    if (progWidget.isItemValidForFilters(drone.getInv().getStackInSlot(i))) return true;
                }
                return false;
            }
            case CLICK_BLOCK -> {
                return DroneAIDig.isBlockValidForFilter(drone.world(), pos, drone, progWidget);
            }
        }
        return false;

    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        visitedPositions.add(pos);

        if (rightClick(pos)) {
            // Successful click. Clear the mainhand item if necessary.
            if (drone.getFakePlayer().getMainHandItem().getCount() <= 0) {
                drone.getFakePlayer().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }

            // Copy mainhand item back from fake player inv to slot 0 of drone's inventory (which always exists)
            drone.getInv().setStackInSlot(0, drone.getFakePlayer().getMainHandItem());

            // Fake player's inventory may have been modified by the right-click action
            // Copy the rest of the fake player's inventory back to the drone's actual inventory,
            // dropping items which don't fit (based on the inventory upgrades the drone has)
            drone.getDroneItemHandler().copyFromFakePlayer();
        }

        // always return false here; the block's been clicked and we don't care about the operation result
        // - just move on to the next block in the area next time
        return false;
    }

    private boolean rightClick(BlockPos pos) {
        if (clickType == IBlockRightClicker.RightClickType.CLICK_ITEM) {
            return rightClickItem(drone.getFakePlayer(), pos);
        } else {
            return rightClickBlock(drone.getFakePlayer(), pos);
        }
    }

    private boolean rightClickItem(FakePlayer fakePlayer, BlockPos pos) {
        // if necessary, find a filter-matching item in the inventory, and swap it into slot 0 (drone's held item)
        if (!progWidget.isItemValidForFilters(drone.getInv().getStackInSlot(0)) && !trySwapItem()) {
            return false;
        }

        ItemStack stack = fakePlayer.getMainHandItem();
        Level world = fakePlayer.getCommandSenderWorld();

        // this is adapted from PlayerInteractionManager#processRightClickBlock()
        try {
            BlockHitResult brtr = doTrace(world, pos, fakePlayer);
            if (brtr == null) return false;

            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, InteractionHand.MAIN_HAND, pos, brtr);
            if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
                return false;
            }

            InteractionResult ret = stack.onItemUseFirst(new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, brtr));
            if (ret != InteractionResult.PASS) return ret.consumesAction();

            if (stack.isEmpty() || fakePlayer.getCooldowns().isOnCooldown(stack.getItem())) {
                return false;
            }

            if (stack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem)stack.getItem()).getBlock();
                if (block instanceof CommandBlock || block instanceof StructureBlock) {
                    return false;
                }
            }

            if (event.getUseItem() != Event.Result.DENY) {
                ItemStack copyBeforeUse = stack.copy();
                InteractionResult result = stack.useOn(new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, brtr));
                if (result == InteractionResult.PASS) {
                    InteractionResultHolder<ItemStack> rightClickResult = stack.getItem().use(world, fakePlayer, InteractionHand.MAIN_HAND);
                    fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, rightClickResult.getObject());
                }
                if (fakePlayer.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(fakePlayer, copyBeforeUse, InteractionHand.MAIN_HAND);
                }
                return true;
            }
        } catch (Throwable e) {
            // crash could happen in right-click logic of item, which could be from any mod...
            Log.error("DroneAIRightClickBlock crashed! Stacktrace: ");
            e.printStackTrace();
        }
        return false;
    }

    private boolean rightClickBlock(FakePlayer fakePlayer, BlockPos pos) {
        Level world = fakePlayer.getCommandSenderWorld();
        BlockState state = world.getBlockState(pos);
        BlockHitResult brtr = doTrace(world, pos, fakePlayer);
        if (brtr != null) {
            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, InteractionHand.MAIN_HAND, pos, brtr);
            try {
                if (!event.isCanceled() && event.getUseItem() != Event.Result.DENY && event.getUseBlock() != Event.Result.DENY) {
                    InteractionResult res = state.use(world, fakePlayer, InteractionHand.MAIN_HAND, brtr);
                    if (res.consumesAction()) {
                        world.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                        return true;
                    }
                }
            } catch (Throwable e) {
                // crash could happen in activated logic of block, which could be from any mod...
                Log.error("DroneAIRightClickBlock crashed! Stacktrace: ");
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean trySwapItem() {
        for (int i = 1; i < drone.getInv().getSlots(); i++) {
            if (progWidget.isItemValidForFilters(drone.getInv().getStackInSlot(i))) {
                ItemStack tmp = drone.getInv().getStackInSlot(i);
                drone.getInv().setStackInSlot(i, drone.getInv().getStackInSlot(0));
                drone.getInv().setStackInSlot(0, tmp);
                return true;
            }
        }
        return false;
    }

    private BlockHitResult doTrace(Level world, BlockPos pos, FakePlayer fakePlayer) {
        BlockState state = world.getBlockState(pos);
        List<AABB> l = state.getShape(world, pos).toAabbs();
        Vec3 targetVec = l.isEmpty() ? Vec3.atCenterOf(pos) : l.get(0).getCenter().add(Vec3.atLowerCornerOf(pos));
        Direction side = ISidedWidget.getDirForSides(((ISidedWidget) progWidget).getSides());
        Vec3 saved = new Vec3(fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ());
        Vec3 posVec = targetVec.add(side.getStepX(), side.getStepY(), side.getStepZ());
        fakePlayer.setPos(posVec.x, posVec.y, posVec.z);
        fakePlayer.lookAt(EntityAnchorArgument.Anchor.FEET, targetVec);
        BlockHitResult brtr = drone.world().clip(new ClipContext(posVec, targetVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, fakePlayer));
        fakePlayer.setPos(saved.x, saved.y, saved.z);
        if (!brtr.getBlockPos().equals(pos) || brtr.getDirection() != side) return null;
        return brtr;
    }
}
