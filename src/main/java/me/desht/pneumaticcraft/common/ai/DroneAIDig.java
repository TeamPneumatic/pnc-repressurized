package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IToolUser;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class DroneAIDig<W extends ProgWidgetAreaItemBase & IToolUser> extends DroneAIBlockInteraction<W> {
    public DroneAIDig(IDroneBase drone, W widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        BlockState blockState = worldCache.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!worldCache.getBlockState(pos).isAir(worldCache, pos) && !ignoreBlock(block) && !pos.equals(drone.getControllerPos())) {
            for (ItemStack droppedStack : getDrops(worldCache, pos, drone)) {
                if (progWidget.isItemValidForFilters(droppedStack, blockState)) {
                    return swapBestItemToFirstSlot(pos) || !progWidget.requiresTool();
                }
            }
            if (progWidget.isItemValidForFilters(ItemStack.EMPTY, blockState)) {
                // try a by-block check
                return swapBestItemToFirstSlot(pos) || !progWidget.requiresTool();
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
        float baseSoftness = worldCache.getBlockState(pos).getPlayerRelativeBlockHardness(drone.getFakePlayer(), drone.world(), pos);
        drone.getInv().setStackInSlot(0, currentStackSaved);
        boolean hasDiggingTool = false;

        // now find the best tool which is better than an empty hand
        int bestSlot = 0;
        float bestSoftness = Float.MIN_VALUE;
        BlockState state = worldCache.getBlockState(pos);
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            drone.getInv().setStackInSlot(0, drone.getInv().getStackInSlot(i));
            float softness = state.getPlayerRelativeBlockHardness(drone.getFakePlayer(), drone.world(), pos);
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
        return hasDiggingTool || !state.getRequiresTool();
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        PlayerInteractionManager manager = drone.getFakePlayer().interactionManager;
        if (!manager.isDestroyingBlock || !manager.receivedFinishDiggingPacket) { //is not destroying and is not acknowledged.
            BlockState blockState = worldCache.getBlockState(pos);
            Block block = blockState.getBlock();
            if (!ignoreBlock(block) && isBlockValidForFilter(worldCache, pos, drone, progWidget)) {
                if (blockState.getBlockHardness(drone.world(), pos) < 0) {
                    addToBlacklist(pos);
                    drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.dig.debug.cantDigBlock", pos);
                    drone.setDugBlock(null);
                    return false;
                }
                int limit = drone.world().getServer().getBuildLimit();
                manager.func_225416_a(pos, CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, Direction.DOWN, limit);
                manager.func_225416_a(pos, CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, Direction.DOWN, limit);
                drone.setDugBlock(pos);
                return true;
            }
            drone.setDugBlock(null);
            return false;
        } else {
            return true;
        }
    }

    public static boolean isBlockValidForFilter(IBlockReader worldCache, BlockPos pos, IDroneBase drone, ProgWidgetAreaItemBase widget) {
        BlockState blockState = worldCache.getBlockState(pos);
        Block block = blockState.getBlock();

        if (!block.isAir(blockState, worldCache, pos)) {
            for (ItemStack droppedStack : getDrops(worldCache, pos, drone)) {
                if (widget.isItemValidForFilters(droppedStack, blockState)) {
                    return true;
                }
            }
            return widget.isItemValidForFilters(ItemStack.EMPTY, blockState);  // try a by-block check
        }
        return false;
    }

    private static List<ItemStack> getDrops(IBlockReader worldCache, BlockPos pos, IDroneBase drone) {
        BlockState state = worldCache.getBlockState(pos);
        return state.getDrops(
                new LootContext.Builder((ServerWorld) drone.world())
                        .withParameter(LootParameters.BLOCK_STATE, state)
                        .withParameter(LootParameters.field_237457_g_, Vector3d.copyCentered(pos))
                        .withParameter(LootParameters.TOOL, drone.getInv().getStackInSlot(0))
        );
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean ignoreBlock(Block block) {
        return PneumaticCraftUtils.isBlockLiquid(block);
    }
}
