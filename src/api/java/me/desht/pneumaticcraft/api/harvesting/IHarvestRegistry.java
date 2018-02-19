package me.desht.pneumaticcraft.api.harvesting;

import java.util.function.Predicate;

import net.minecraft.block.state.IBlockState;

/**
 * Registry for registering harvest handlers. Note that any subclass of {@link net.minecraft.block.BlockCrops} is supported automatically.
 * @author MineMaarten
 *
 */
public interface IHarvestRegistry{
    /**
     * Registers a generic harvest handler.
     * @param harvestHandler
     */
    public void registerHarvestHandler(IHarvestHandler harvestHandler);
    
    /**
     * Registers a harvest handler for block states that need to be farmed like cactusses/sugar canes,
     * in that the top blocks can be harvested as long as there is a plant block left at the bottom.
     * @param blockChecker return true if the given block state is a state you target.
     */
    public void registerHarvestHandlerCactuslike(Predicate<IBlockState> blockChecker);
}
