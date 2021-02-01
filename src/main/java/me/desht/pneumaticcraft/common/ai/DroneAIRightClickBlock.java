package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IBlockRightClicker;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetBlockRightClick;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
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

        if (widget instanceof IBlockRightClicker) {
            drone.getFakePlayer().setSneaking(((IBlockRightClicker) widget).isSneaking());
            clickType = ((IBlockRightClicker)widget).getClickType();
        } else {
            throw new IllegalArgumentException("expecting a widget implementing IBlockRightClicker!");
        }
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (visitedPositions.contains(pos)) return false;
        if (progWidget.isItemFilterEmpty()) return true;
        switch (clickType) {
            case CLICK_ITEM:
                for (int i = 0; i < drone.getInv().getSlots(); i++) {
                    if (progWidget.isItemValidForFilters(drone.getInv().getStackInSlot(i))) return true;
                }
                return false;
            case CLICK_BLOCK:
                return DroneAIDig.isBlockValidForFilter(drone.world(), pos, drone, progWidget);
        }
        return false;

    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
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
        if (clickType == IBlockRightClicker.RightClickType.CLICK_ITEM) {
            rightClickItem(drone.getFakePlayer(), pos);
        } else {
            rightClickBlock(drone.getFakePlayer(), pos);
        }
    }

    private void rightClickItem(FakePlayer fakePlayer, BlockPos pos) {
        // if necessary, find a filter-matching item in the inventory, and swap it into slot 0 (drone's held item)
        if (!progWidget.isItemValidForFilters(drone.getInv().getStackInSlot(0)) && !trySwapItem()) return;

        ItemStack stack = fakePlayer.getHeldItemMainhand();
        World world = fakePlayer.getEntityWorld();

        // this is adapted from PlayerInteractionManager#processRightClickBlock()
        try {
            BlockRayTraceResult brtr = doTrace(world, pos, fakePlayer);
            if (brtr == null) return;

            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, Hand.MAIN_HAND, pos, brtr);
            if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
                return;
            }

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

            // At this point, it's possible that something the drone has done has added items to the fake player's inventory
            // in slots beyond the drone's effective inventory size.
            // We can't prevent this, so look for any such items, and drop them on the ground.
            PlayerInventory fakeInv = fakePlayer.inventory;
            if (drone.getInv().getSlots() < fakeInv.getSizeInventory() && !fakeInv.getStackInSlot(drone.getInv().getSlots()).isEmpty()) {
                for (int slot = drone.getInv().getSlots(); slot < fakeInv.getSizeInventory(); slot++) {
                    ItemStack stack1 = fakeInv.getStackInSlot(slot);
                    if (!stack1.isEmpty()) {
                        PneumaticCraftUtils.dropItemOnGround(stack1, drone.world(), new BlockPos(drone.getDronePos()));
                        fakeInv.setInventorySlotContents(slot, ItemStack.EMPTY);
                    }
                }
            }
        } catch (Throwable e) {
            // crash could happen in right-click logic of item, which could be from any mod...
            Log.error("DroneAIRightClickBlock crashed! Stacktrace: ");
            e.printStackTrace();
        }
    }

    private void rightClickBlock(FakePlayer fakePlayer, BlockPos pos) {
        World world = fakePlayer.getEntityWorld();
        BlockState state = world.getBlockState(pos);
        BlockRayTraceResult brtr = doTrace(world, pos, fakePlayer);
        if (brtr == null) return;
        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, Hand.MAIN_HAND, pos, brtr);

        try {
            if (!event.isCanceled() && event.getUseItem() != Event.Result.DENY && event.getUseBlock() != Event.Result.DENY) {
                ActionResultType res = state.onBlockActivated(world, fakePlayer, Hand.MAIN_HAND, brtr);
                if (res.isSuccessOrConsume()) {
                    world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.DEFAULT);
                }
            }
        } catch (Throwable e) {
            // crash could happen in activated logic of block, which could be from any mod...
            Log.error("DroneAIRightClickBlock crashed! Stacktrace: ");
            e.printStackTrace();
        }
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

    private BlockRayTraceResult doTrace(World world, BlockPos pos, FakePlayer fakePlayer) {
        BlockState state = world.getBlockState(pos);
        List<AxisAlignedBB> l = state.getShape(world, pos).toBoundingBoxList();
        Vector3d targetVec = l.isEmpty() ? Vector3d.copyCentered(pos) : l.get(0).getCenter().add(Vector3d.copy(pos));
        Direction side = ((ProgWidgetBlockRightClick) progWidget).getClickSide();
        Vector3d posVec = targetVec.add(side.getXOffset(), side.getYOffset(), side.getZOffset());
        fakePlayer.setPosition(posVec.x, posVec.y, posVec.z);
        fakePlayer.lookAt(EntityAnchorArgument.Type.FEET, targetVec);
        BlockRayTraceResult brtr = drone.world().rayTraceBlocks(new RayTraceContext(posVec, targetVec, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.SOURCE_ONLY, fakePlayer));
        if (!brtr.getPos().equals(pos) || brtr.getFace() != side) return null;
        return brtr;
    }
}
