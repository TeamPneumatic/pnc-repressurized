package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IBlockRightClicker;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetPlace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

public class DroneAIBlockInteract extends DroneAIBlockInteraction {

    private final List<BlockPos> visitedPositions = new ArrayList<BlockPos>();

    public DroneAIBlockInteract(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
        drone.getFakePlayer().setSneaking(((IBlockRightClicker) widget).isSneaking());
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return !visitedPositions.contains(pos) && (widget.isItemFilterEmpty() || DroneAIDig.isBlockValidForFilter(drone.world(), drone, pos, widget));
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        visitedPositions.add(pos);
        boolean result = rightClick(pos);
        if (drone.getFakePlayer().getHeldItemMainhand().getCount() <= 0) {
            drone.getFakePlayer().setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        }
        return result;
    }

    private boolean rightClick(BlockPos pos) {
        EnumFacing faceDir = ProgWidgetPlace.getDirForSides(((ISidedWidget) widget).getSides());
        EntityPlayer player = drone.getFakePlayer();
        World world = drone.world();
        ItemStack stack = player.getHeldItemMainhand();

        player.setPosition(pos.getX() + 0.5, pos.getY() + 0.5 - player.eyeHeight, pos.getZ() + 0.5);
        player.rotationPitch = faceDir.getFrontOffsetY() * -90;
        player.rotationYaw = PneumaticCraftUtils.getYawFromFacing(faceDir);

        float hitX = (float)(player.posX - pos.getX());
        float hitY = (float)(player.posY - pos.getY());
        float hitZ = (float)(player.posZ - pos.getZ());

        // this is adapted from PlayerInteractionManager#processRightClickBlock()
        try {
            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, EnumHand.MAIN_HAND, pos, faceDir.getOpposite(),  ForgeHooks.rayTraceEyeHitVec(player, 2.0D));
            if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
                return false;
            }

            EnumActionResult ret = stack.onItemUseFirst(player, world, pos, EnumHand.MAIN_HAND, faceDir, hitX, hitY, hitZ);
            if (ret != EnumActionResult.PASS) return false;

            boolean bypass = player.getHeldItemMainhand().doesSneakBypassUse(world, pos, player);
            EnumActionResult result = EnumActionResult.PASS;

            if (!player.isSneaking() || bypass || event.getUseBlock() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
                IBlockState iblockstate = world.getBlockState(pos);
                if(event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                    if (iblockstate.getBlock().onBlockActivated(world, pos, iblockstate, player, EnumHand.MAIN_HAND, faceDir, hitX, hitY, hitZ)) {
                        result = EnumActionResult.SUCCESS;
                    }
            }

            if (stack.isEmpty() || player.getCooldownTracker().hasCooldown(stack.getItem())) {
                return false;
            }

            if (stack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
                Block block = ((ItemBlock)stack.getItem()).getBlock();
                if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
                    return false;
                }
            }

            if (result != EnumActionResult.SUCCESS && event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY
                    || result == EnumActionResult.SUCCESS && event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
                ItemStack copyBeforeUse = stack.copy();
                result = stack.onItemUse(player, world, pos, EnumHand.MAIN_HAND, faceDir, hitX, hitY, hitZ);
                if (result == EnumActionResult.PASS) {
                    ActionResult<ItemStack> rightClickResult = stack.getItem().onItemRightClick(world, player, EnumHand.MAIN_HAND);
                    player.setHeldItem(EnumHand.MAIN_HAND, rightClickResult.getResult());
                }
                if (player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, EnumHand.MAIN_HAND);
                }
            }

            return false;
        } catch (Throwable e) {
            Log.error("DroneAIBlockInteract crashed! Stacktrace: ");
            e.printStackTrace();
            return false;
        }
    }

}
