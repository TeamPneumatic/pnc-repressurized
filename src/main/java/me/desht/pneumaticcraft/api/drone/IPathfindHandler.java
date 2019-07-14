package me.desht.pneumaticcraft.api.drone;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Implement this and register it via {@link IDroneRegistry#addPathfindableBlock(Block, IPathfindHandler)} to provide
 * custom pathfinding functionality for a particular block.
 */
public interface IPathfindHandler {
    /**
     * Check if the drone may pathfind through the block at the given world and block position.
     *
     * @param world the drone's world
     * @param pos the block position to test
     * @return true if the drone may pathfind through this block, false otherwise
     */
    boolean canPathfindThrough(World world, BlockPos pos);

    /**
     * CURRENTLY NOT IMPLEMENTED!
     * Will be called every tick as long as the drone is < 1 block away from the given coordinate.
     * can be used to open a door for a drone for example.
     *
     * @param world the drone's world
     * @param pos the block position to test
     */
    void onPathingThrough(World world, BlockPos pos);
}
