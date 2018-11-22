package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IToolUser;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

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
        IBlockState blockState = worldCache.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!worldCache.isAirBlock(pos) && !ignoreBlock(block)) {
            NonNullList<ItemStack> droppedStacks = NonNullList.create();
            if (block.canSilkHarvest(drone.world(), pos, blockState, drone.getFakePlayer())) {
                droppedStacks.add(getSilkTouchBlock(block, blockState));
            } else {
                block.getDrops(droppedStacks, drone.world(), pos, blockState, 0);
            }
            for (ItemStack droppedStack : droppedStacks) {
                if (widget.isItemValidForFilters(droppedStack, blockState)) {
                    return swapBestItemToFirstSlot(pos) || !((IToolUser)widget).requiresTool();
                }
            }
            if (widget.isItemValidForFilters(ItemStack.EMPTY, blockState)) {
                // try a by-block check
                return swapBestItemToFirstSlot(pos) || !((IToolUser)widget).requiresTool();
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
            IBlockState blockState = worldCache.getBlockState(pos);
            Block block = blockState.getBlock();
            if (!ignoreBlock(block) && isBlockValidForFilter(worldCache, drone, pos, widget)) {
                if (blockState.getBlockHardness(drone.world(), pos) < 0) {
                    addToBlacklist(pos);
                    drone.addDebugEntry("gui.progWidget.dig.debug.cantDigBlock", pos);
                    drone.setDugBlock(null);
                    return false;
                }
                manager.onBlockClicked(pos, EnumFacing.DOWN);
                manager.blockRemoving(pos);
                /*if (!manager.isDestroyingBlock) { Commenting this a fix for #142? This statement always holds when blockRemoving(pos) is called.
                    addToBlacklist(pos);
                    drone.addDebugEntry("gui.progWidget.dig.debug.cantDigBlock", pos);
                    drone.setDugBlock(null);
                    return false;
                }*/
                drone.setDugBlock(pos);
                return true;
            }
            drone.setDugBlock(null);
            return false;
        } else {
            return true;
        }
    }

    public static boolean isBlockValidForFilter(IBlockAccess worldCache, IDroneBase drone, BlockPos pos, ProgWidgetAreaItemBase widget) {
        IBlockState blockState = worldCache.getBlockState(pos);
        Block block = blockState.getBlock();

        if (!block.isAir(blockState, worldCache, pos)) {
            NonNullList<ItemStack> droppedStacks = NonNullList.create();
            if (block.canSilkHarvest(drone.world(), pos, blockState, drone.getFakePlayer())) {
                droppedStacks.add(getSilkTouchBlock(block, blockState));
            } else {
                block.getDrops(droppedStacks, drone.world(), pos, blockState, 0);
            }
            for (ItemStack droppedStack : droppedStacks) {
                if (widget.isItemValidForFilters(droppedStack, blockState)) {
                    return true;
                }
            }
            return widget.isItemValidForFilters(ItemStack.EMPTY, blockState);  // try a by-block check
        }
        return false;
    }

    @Nonnull
    private static ItemStack getSilkTouchBlock(Block block, IBlockState state) {
        Item item = Item.getItemFromBlock(block);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        } else {
            return new ItemStack(item, 1, block.getMetaFromState(state));
        }
    }

    private static boolean ignoreBlock(Block block) {
        return PneumaticCraftUtils.isBlockLiquid(block);
    }

}
