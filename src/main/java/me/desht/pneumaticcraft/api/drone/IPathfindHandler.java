package me.desht.pneumaticcraft.api.drone;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPathfindHandler {
    /**
     * When returned true, the drone can pathfind through this block. When false it can't.
     *
     * @param world
     * @param pos
     * @return
     */
    boolean canPathfindThrough(World world, BlockPos pos);

    /**
     * CURRENTLY NOT IMPLEMENTED!
     * Will be called every tick as long as the drone is < 1 block away from the given coordinate.
     * can be used to open a door for a drone for example.
     *
     * @param world
     * @param pos
     */
    void onPathingThrough(World world, BlockPos pos);
}
