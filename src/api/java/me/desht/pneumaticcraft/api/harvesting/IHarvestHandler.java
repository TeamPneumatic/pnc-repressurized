package me.desht.pneumaticcraft.api.harvesting;

import me.desht.pneumaticcraft.api.drone.IDrone;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Defines a generic harvest handler.
 * @author MineMaarten
 *
 */
@FunctionalInterface
public interface IHarvestHandler{

    /**
     * Should actually harvest the block. Usually this is just a matter of breaking the block, but you can override this for custom behavior.
     * @param world
     * @param chunkCache Use preferably methods from this cache as it's generally quicker than accessing via 'world'. 
     * The cache has access to the chunks that are accessed by the Drone current program, so as long as only the y pos is varied of the
     * supplied pos, you are good. If not, use 'world'.
     * @param pos
     * @param state
     * @param drone
     * @return
     */
    public default void harvest(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, IDrone drone){
        world.destroyBlock(pos, true);
    }
    
    /**
     * Should harvest the block (drop items), and replant the plant if applicable. For example, for crops it should reset the growth stage to 0.
     * @param world
     * @param chunkCache Use preferably methods from this cache as it's generally quicker than accessing via 'world'. 
     * The cache has access to the chunks that are accessed by the Drone current program, so as long as only the y pos is varied of the
     * supplied pos, you are good. If not, use 'world'.
     * @param pos
     * @param state
     * @param drone
     * @return true if the replanting succeeded (and the hoe the Drone carries needs to be damaged). If nothing needed to be replanted return false.
     */
    public default boolean harvestAndReplant(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, IDrone drone){
        harvest(world, chunkCache, pos, state, drone);
        return false;
    }
    
    /**
     * Determines if the currently checked block can be harvested.
     * @param world
     * @param chunkCache Use preferably methods from this cache as it's generally quicker than accessing via 'world'. 
     * The cache has access to the chunks that are accessed by the Drone current program, so as long as only the y pos is varied of the
     * supplied pos, you are good. If not, use 'world'.
     * @param pos
     * @param state
     * @param drone
     * @return true if the block can be harvested, false if not.
     */
    public boolean canHarvest(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, IDrone drone);
    
    /**
     * Should add the items the connected item filters in the Harvest puzzle piece in the Programmer can use to determine if a block should be harvested.
     * Called after {@link IHarvestHandler#canHarvest(World, IBlockAccess, BlockPos, IBlockState, boolean)}, when that method returns true
     * @param world
     * @param chunkCache Use preferably methods from this cache as it's generally quicker than accessing via 'world'. 
     * The cache has access to the chunks that are accessed by the Drone current program, so as long as only the y pos is varied of the
     * supplied pos, you are good. If not, use 'world'.
     * @param pos
     * @param state
     * @param stacks
     * @param drone
     */
    public default void addFilterItems(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, NonNullList<ItemStack> stacks, IDrone drone){
        Block block = state.getBlock();
        block.getDrops(stacks, chunkCache, pos, state, 0);
    }
}
