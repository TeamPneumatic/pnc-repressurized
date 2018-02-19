package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.harvesting.IHarvestHandler;
import me.desht.pneumaticcraft.common.harvesting.HarvestRegistry;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

public class DroneAIHarvest extends DroneAIBlockInteraction<ProgWidgetAreaItemBase> {

    /**
     * @param drone the drone
     * @param widget needs to implement IBlockOrdered
     */
    public DroneAIHarvest(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return getApplicableHandler(pos) != null;
    }
    
    private IHarvestHandler getApplicableHandler(BlockPos pos){
        IBlockState state = worldCache.getBlockState(pos); 
        return HarvestRegistry.getInstance()
                              .getHarvestHandlers()
                              .stream()
                              .filter(handler -> handler.canHarvest(drone.world(), worldCache, pos, state) &&
                                                  hasApplicableItemFilters(handler, pos, state))
                              .findFirst()
                              .orElse(null);
    }
    
    private boolean hasApplicableItemFilters(IHarvestHandler harvestHandler, BlockPos pos, IBlockState blockState){
        NonNullList<ItemStack> droppedStacks = NonNullList.create();
        
        harvestHandler.addFilterItems(drone.world(), worldCache, pos, blockState, droppedStacks);
        
        for (ItemStack droppedStack : droppedStacks) {
            if (widget.isItemValidForFilters(droppedStack, blockState)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean respectClaims() {
        return true;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        IHarvestHandler applicableHandler = getApplicableHandler(pos);
        if(applicableHandler != null){
            IBlockState state = worldCache.getBlockState(pos);
            if(applicableHandler.canHarvest(drone.world(), worldCache, pos, state)){
                applicableHandler.harvest(drone.world(), worldCache, pos, state);
            }
        }
        return false;
    }
}
