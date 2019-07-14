package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IToolUser;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;

import java.util.List;

public class DroneAIDig extends DroneAIBlockInteraction<ProgWidgetAreaItemBase> {

    /**
     * @param drone the drone
     * @param widget needs to implement IBlockOrdered, IToolUser
     */
    public DroneAIDig(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        BlockState blockState = worldCache.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!worldCache.isAirBlock(pos) && !ignoreBlock(block)) {
            for (ItemStack droppedStack : getDrops(worldCache, pos, drone)) {
                if (progWidget.isItemValidForFilters(droppedStack, blockState)) {
                    return swapBestItemToFirstSlot(pos) || !((IToolUser) progWidget).requiresTool();
                }
            }
            if (progWidget.isItemValidForFilters(ItemStack.EMPTY, blockState)) {
                // try a by-block check
                return swapBestItemToFirstSlot(pos) || !((IToolUser) progWidget).requiresTool();
            }
        }
        return false;
    }

    @Override
    protected boolean respectClaims() {
        return true;
    }

    //gui.progWidget.dig.debug.missingDiggingTool
    private boolean swapBestItemToFirstSlot(BlockPos pos) {
        
        ItemStack oldCurrentStack = drone.getInv().getStackInSlot(0).copy();
        drone.getInv().setStackInSlot(0, ItemStack.EMPTY);
        float baseSoftness = worldCache.getBlockState(pos).getPlayerRelativeBlockHardness(drone.getFakePlayer(), drone.world(), pos);
        drone.getInv().setStackInSlot(0, oldCurrentStack);
        boolean hasDiggingTool = false;
        
        int bestSlot = 0;
        float bestSoftness = Float.MIN_VALUE;
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            drone.getInv().setStackInSlot(0, drone.getInv().getStackInSlot(i));
            float softness = worldCache.getBlockState(pos).getPlayerRelativeBlockHardness(drone.getFakePlayer(), drone.world(), pos);
            if (softness > bestSoftness) {
                bestSlot = i;
                bestSoftness = softness;
                
                if(softness > baseSoftness){
                    hasDiggingTool = true;
                }
            }
        }
        drone.getInv().setStackInSlot(0, oldCurrentStack);
        if (bestSlot != 0) {
            ItemStack bestItem = drone.getInv().getStackInSlot(bestSlot).copy();
            drone.getInv().setStackInSlot(bestSlot, drone.getInv().getStackInSlot(0));
            drone.getInv().setStackInSlot(0, bestItem);
        }
        return hasDiggingTool;
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
                    drone.addDebugEntry("gui.progWidget.dig.debug.cantDigBlock", pos);
                    drone.setDugBlock(null);
                    return false;
                }
                manager.startDestroyBlock(pos, Direction.DOWN);
                manager.stopDestroyBlock(pos);
                drone.setDugBlock(pos);
                return true;
            }
            drone.setDugBlock(null);
            return false;
        } else {
            return true;
        }
    }

    public static boolean isBlockValidForFilter(IWorldReader worldCache, BlockPos pos, IDroneBase drone, ProgWidgetAreaItemBase widget) {
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

    private static List<ItemStack> getDrops(IWorldReader worldCache, BlockPos pos, IDroneBase drone) {
        return worldCache.getBlockState(pos).getDrops(
                new LootContext.Builder((ServerWorld) drone.world())
                        .withParameter(LootParameters.POSITION, pos)
                        .withParameter(LootParameters.TOOL, drone.getInv().getStackInSlot(0))
        );
    }

    private static boolean ignoreBlock(Block block) {
        return PneumaticCraftUtils.isBlockLiquid(block);
    }

}
