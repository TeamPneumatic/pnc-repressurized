package me.desht.pneumaticcraft.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blocks can implement this interface to indicate that they can connect networked pressure tube
 * modules, such as Redstone Modules and Logistics Modules.
 * By default, only Pressure Tube blocks implement this.
 */
public interface ITubeNetworkConnector {
    /**
     * Called when a network is being scanned. Check if the block at the given position can connect to a tube
     * module network in the given direction. This may be as simple as just checking a blockstate property;
     * the blockstate at the position is passed for convenience. If that is not sufficient, the level and position are
     * available in case of the need for a block entity query.
     *
     * @param level the world
     * @param pos block position in question
     * @param dir the direction to be connected to
     * @param state the blockstate at the given position
     * @return true if this block should form part of the module network, false otherwise
     */
    boolean canConnectToNetwork(Level level, BlockPos pos, Direction dir, BlockState state);
}
