package me.desht.pneumaticcraft.api.harvesting;

import me.desht.pneumaticcraft.api.drone.IDrone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

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
    default void harvest(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone){
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
    default boolean harvestAndReplant(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone){
        harvest(world, chunkCache, pos, state, drone);
        return false;
    }
    
    /**
     * Determines if the currently checked block can be harvested.
     * @param world the world
     * @param chunkCache Use preferably methods from this cache as it's generally quicker than accessing via 'world'. 
     * The cache has access to the chunks that are accessed by the Drone current program, so as long as only the y pos is varied of the
     * supplied pos, you are good. If not, use 'world'.
     * @param pos the blockpos to be checked
     * @param state the blockstate
     * @param drone the drone
     * @return true if the block can be harvested, false if not.
     */
    boolean canHarvest(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone);
    
    /**
     * Should add the items the connected item filters in the Harvest puzzle piece in the Programmer can use to determine if a block should be harvested.
     * Called after {@link IHarvestHandler#canHarvest(World, IBlockReader, BlockPos, BlockState, IDrone)}, when that method returns true
     * @param world the world
     * @param chunkCache Use preferably methods from this cache as it's generally quicker than accessing via 'world'. 
     * The cache has access to the chunks that are accessed by the Drone current program, so as long as only the y pos is varied of the
     * supplied pos, you are good. If not, use 'world'.
     * @param pos the blockpos
     * @param state the blockstate
     * @param drone the drone
     */
    default List<ItemStack> addFilterItems(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone){
        return world instanceof ServerWorld ? Block.getDrops(state, (ServerWorld) world, pos, world.getTileEntity(pos)) : Collections.emptyList();
    }
}
