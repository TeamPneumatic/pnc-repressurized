package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IBlockRightClicker;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetBlockRightClick;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class DroneAIRightClickBlock extends DroneAIBlockInteraction<ProgWidgetBlockRightClick> {
    private final List<BlockPos> visitedPositions = new ArrayList<>();

    public DroneAIRightClickBlock(IDroneBase drone, ProgWidgetBlockRightClick widget) {
        super(drone, widget);

        drone.getFakePlayer().setSneaking(((IBlockRightClicker) widget).isSneaking());
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return !visitedPositions.contains(pos) &&
                (progWidget.isItemFilterEmpty() || DroneAIDig.isBlockValidForFilter(drone.world(), pos, drone, progWidget));
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        visitedPositions.add(pos);
        rightClick(pos);
        if (drone.getFakePlayer().getHeldItemMainhand().getCount() <= 0) {
            drone.getFakePlayer().setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        drone.getInv().setStackInSlot(0, drone.getFakePlayer().getHeldItemMainhand());

        // always return false here; the block's been clicked and we don't care about the operation result
        // - just move on to the next block in the area next time
        return false;
    }

    private void rightClick(BlockPos pos) {
        Direction faceDir = ISidedWidget.getDirForSides(((ISidedWidget) progWidget).getSides());
        if (progWidget.getClickType() == IBlockRightClicker.RightClickType.CLICK_ITEM) {
            rightClickItem(drone.getFakePlayer(), pos, faceDir);
        } else {
            rightClickBlock(drone.getFakePlayer(), pos, faceDir);
        }
    }

    private void rightClickItem(FakePlayer fakePlayer, BlockPos pos, Direction faceDir) {
        ItemStack stack = fakePlayer.getHeldItemMainhand();
        World world = fakePlayer.getEntityWorld();

        // this is adapted from PlayerInteractionManager#processRightClickBlock()
        try {
            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, Hand.MAIN_HAND, pos, faceDir);
            if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
                return;
            }

            BlockRayTraceResult brtr = doTrace(world, pos, fakePlayer);
            if (brtr == null) return;

            ActionResultType ret = stack.onItemUseFirst(new ItemUseContext(fakePlayer, Hand.MAIN_HAND, brtr));
            if (ret != ActionResultType.PASS) return;

            if (stack.isEmpty() || fakePlayer.getCooldownTracker().hasCooldown(stack.getItem())) {
                return;
            }

            if (stack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem)stack.getItem()).getBlock();
                if (block instanceof CommandBlockBlock || block instanceof StructureBlock) {
                    return;
                }
            }

            if (event.getUseItem() != Event.Result.DENY) {
                ItemStack copyBeforeUse = stack.copy();
                ActionResultType result = stack.onItemUse(new ItemUseContext(fakePlayer, Hand.MAIN_HAND, brtr));
                if (result == ActionResultType.PASS) {
                    ActionResult<ItemStack> rightClickResult = stack.getItem().onItemRightClick(world, fakePlayer, Hand.MAIN_HAND);
                    fakePlayer.setHeldItem(Hand.MAIN_HAND, rightClickResult.getResult());
                }
                if (fakePlayer.getHeldItem(Hand.MAIN_HAND).isEmpty()) {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(fakePlayer, copyBeforeUse, Hand.MAIN_HAND);
                }
            }
        } catch (Throwable e) {
            Log.error("DroneAIBlockInteract crashed! Stacktrace: ");
            e.printStackTrace();
        }
    }

    private void rightClickBlock(FakePlayer fakePlayer, BlockPos pos, Direction faceDir) {
        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, Hand.MAIN_HAND, pos, faceDir);
        if (!event.isCanceled() && event.getUseItem() != Event.Result.DENY && event.getUseBlock() != Event.Result.DENY) {
            World world = fakePlayer.getEntityWorld();
            BlockState state = world.getBlockState(pos);
            BlockRayTraceResult brtr = doTrace(world, pos, fakePlayer);
            if (brtr != null) {
                ActionResultType res = state.onBlockActivated(world, fakePlayer, Hand.MAIN_HAND, brtr);
                if (res.isSuccessOrConsume()) {
                    world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.DEFAULT);
                }
            }
        }
    }

    private BlockRayTraceResult doTrace(World world, BlockPos pos, FakePlayer fakePlayer) {
        BlockState state = world.getBlockState(pos);
        List<AxisAlignedBB> l = state.getShape(world, pos).toBoundingBoxList();
        if (l.isEmpty()) return null;
        Vec3d vec = l.get(0).getCenter().add(new Vec3d(pos));
        BlockRayTraceResult brtr = drone.world().rayTraceBlocks(new RayTraceContext(drone.getDronePos(), vec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, fakePlayer));
        if (!brtr.getPos().equals(pos)) return null;
        return brtr;
    }
}
