package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IBlockRightClicker;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetBlockRightClick;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
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
        boolean result = rightClick(pos);
        if (drone.getFakePlayer().getHeldItemMainhand().getCount() <= 0) {
            drone.getFakePlayer().setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        drone.getInv().setStackInSlot(0, drone.getFakePlayer().getHeldItemMainhand());
        // note the negation here: return false if the operation succeeded, to indicate we're done
        return !result;
    }

    private boolean rightClick(BlockPos pos) {
        Direction faceDir = ISidedWidget.getDirForSides(((ISidedWidget) progWidget).getSides());
        PlayerEntity fakePlayer = drone.getFakePlayer();
        BlockPos pos2 = pos.offset(faceDir);
        fakePlayer.setPosition(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);
        fakePlayer.rotationPitch = faceDir.getOpposite().getYOffset() * -90;
        fakePlayer.rotationYaw = PneumaticCraftUtils.getYawFromFacing(faceDir.getOpposite());

        return progWidget.getClickType() == IBlockRightClicker.RightClickType.CLICK_ITEM ?
                rightClickItem(drone.getFakePlayer(), pos, faceDir) :
                rightClickBlock(drone.getFakePlayer(), pos, faceDir);
    }

    private boolean rightClickItem(FakePlayer fakePlayer, BlockPos pos, Direction faceDir) {
        ItemStack stack = fakePlayer.getHeldItemMainhand();
        World world = fakePlayer.getEntityWorld();

        // this is adapted from PlayerInteractionManager#processRightClickBlock()
        try {
            Vec3d blockVec = PneumaticCraftUtils.getBlockCentre(pos);
            BlockRayTraceResult brtr = drone.world().rayTraceBlocks(new RayTraceContext(fakePlayer.getPositionVec(), blockVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, fakePlayer));
            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, Hand.MAIN_HAND, pos, faceDir);
            if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
                return false;
            }

            ActionResultType ret = stack.onItemUseFirst(new ItemUseContext(fakePlayer, Hand.MAIN_HAND, brtr));
            if (ret != ActionResultType.PASS) return false;

            boolean bypass = fakePlayer.getHeldItemMainhand().doesSneakBypassUse(world, pos, fakePlayer);
            ActionResultType result = ActionResultType.PASS;

            if (!fakePlayer.isSneaking() || bypass || event.getUseBlock() == Event.Result.ALLOW) {
                if (event.getUseBlock() != Event.Result.DENY) {
                    if (world.getBlockState(pos).onBlockActivated(world, fakePlayer, Hand.MAIN_HAND, brtr) == ActionResultType.SUCCESS) {
                        result = ActionResultType.SUCCESS;
                    }
                }
            }

            if (stack.isEmpty() || fakePlayer.getCooldownTracker().hasCooldown(stack.getItem())) {
                return false;
            }

            if (stack.getItem() instanceof BlockItem && !fakePlayer.canUseCommandBlock()) {
                Block block = ((BlockItem)stack.getItem()).getBlock();
                if (block instanceof CommandBlockBlock || block instanceof StructureBlock) {
                    return false;
                }
            }

            if (result != ActionResultType.SUCCESS && event.getUseItem() != Event.Result.DENY
                    || result == ActionResultType.SUCCESS && event.getUseItem() == Event.Result.ALLOW) {
                ItemStack copyBeforeUse = stack.copy();
                result = stack.onItemUse(new ItemUseContext(fakePlayer, Hand.MAIN_HAND, brtr));
                if (result == ActionResultType.PASS) {
                    ActionResult<ItemStack> rightClickResult = stack.getItem().onItemRightClick(world, fakePlayer, Hand.MAIN_HAND);
                    fakePlayer.setHeldItem(Hand.MAIN_HAND, rightClickResult.getResult());
                }
                if (fakePlayer.getHeldItem(Hand.MAIN_HAND).isEmpty()) {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(fakePlayer, copyBeforeUse, Hand.MAIN_HAND);
                }
            }

            return false;
        } catch (Throwable e) {
            Log.error("DroneAIBlockInteract crashed! Stacktrace: ");
            e.printStackTrace();
            return false;
        }
    }

    private boolean rightClickBlock(FakePlayer fakePlayer, BlockPos pos, Direction faceDir) {
        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, Hand.MAIN_HAND, pos, faceDir);
        if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
            return false;
        }
        if (event.getUseBlock() != Event.Result.DENY) {
            World world = fakePlayer.getEntityWorld();
            BlockState iblockstate = world.getBlockState(pos);
            Vec3d blockVec = PneumaticCraftUtils.getBlockCentre(pos);
            BlockRayTraceResult brtr = drone.world().rayTraceBlocks(new RayTraceContext(fakePlayer.getPositionVec(), blockVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, fakePlayer));
            return iblockstate.onBlockActivated(world, fakePlayer, Hand.MAIN_HAND, brtr) == ActionResultType.SUCCESS;

        }
        return false;
    }

}
