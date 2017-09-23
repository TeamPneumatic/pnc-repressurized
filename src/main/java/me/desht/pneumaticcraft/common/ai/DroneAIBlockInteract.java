package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IBlockRightClicker;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetPlace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

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
        transferToDroneFromFakePlayer(drone);
        return result;
    }

    public static void transferToDroneFromFakePlayer(IDroneBase drone) {
        for (int j = 1; j < drone.getFakePlayer().inventory.mainInventory.size(); j++) {
            ItemStack excessStack = drone.getFakePlayer().inventory.mainInventory.get(j);
            if (!excessStack.isEmpty()) {
                ItemStack remainder = PneumaticCraftUtils.exportStackToInventory(drone, excessStack, null);
                if (!remainder.isEmpty()) {
                    drone.dropItem(remainder);
                }
                drone.getFakePlayer().inventory.mainInventory.set(j, ItemStack.EMPTY);
            }
        }

    }

    private boolean rightClick(BlockPos pos) {

        EnumFacing faceDir = ProgWidgetPlace.getDirForSides(((ISidedWidget) widget).getSides());
        EntityPlayer player = drone.getFakePlayer();
        World world = drone.world();
        int dx = faceDir.getFrontOffsetX();
        int dy = faceDir.getFrontOffsetY();
        int dz = faceDir.getFrontOffsetZ();

        player.setPosition(pos.getX() + 0.5, pos.getY() + 0.5 - player.eyeHeight, pos.getZ() + 0.5);
        player.rotationPitch = faceDir.getFrontOffsetY() * -90;
        switch (faceDir) {
            case NORTH:
                player.rotationYaw = 180;
                break;
            case SOUTH:
                player.rotationYaw = 0;
                break;
            case WEST:
                player.rotationYaw = 90;
                break;
            case EAST:
                player.rotationYaw = -90;
        }

        try {
            PlayerInteractEvent.RightClickEmpty event = new PlayerInteractEvent.RightClickEmpty(player, EnumHand.MAIN_HAND);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) return false;

            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem().onItemUseFirst(player, world, pos, faceDir, dx, dy, dz, EnumHand.MAIN_HAND) == EnumActionResult.PASS)
                return false;

            if (!world.isAirBlock(pos) && block.onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, faceDir, dx, dy, dz))
                return false;

            if (!stack.isEmpty()) {
                boolean isGoingToShift = false;
                if (stack.getItem() == Items.REEDS || stack.getItem() instanceof ItemRedstone) {
                    isGoingToShift = true;
                }
                if (stack.getItem().onItemUse(player, world, pos, EnumHand.MAIN_HAND, faceDir, dx, dy, dz) == EnumActionResult.PASS)
                    return false;

                ItemStack copy = stack.copy();
                ActionResult<ItemStack> res = stack.getItem().onItemRightClick(world, player, EnumHand.MAIN_HAND);
                player.setHeldItem(EnumHand.MAIN_HAND, res.getResult());
                if (!copy.isItemEqual(stack)) return true;
            }
            return false;
        } catch (Throwable e) {
            Log.error("DroneAIBlockInteract crashed! Stacktrace: ");
            e.printStackTrace();
            return false;
        }
    }

}
