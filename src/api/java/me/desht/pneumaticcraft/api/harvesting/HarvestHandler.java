/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.harvesting;

import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.api.drone.IDrone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Defines a generic harvest handler.  Register new harvest handlers via Forge registry events:
 * {@link net.minecraftforge.event.RegistryEvent.Register}
 *
 * @author MineMaarten, desht
 */
public abstract class HarvestHandler extends ForgeRegistryEntry<HarvestHandler> {

    /**
     * Should actually harvest the block. Usually this is just a matter of breaking the block, but you can override this for custom behavior.
     * @param world the world
     * @param chunkCache Use preferably methods from this cache as it's generally quicker than accessing via 'world'. 
     * The cache has access to the chunks that are accessed by the Drone current program, so as long as only the y pos is varied of the
     * supplied pos, you are good. If not, use 'world'.
     * @param pos the block's position
     * @param state the blockstate at the position
     * @param drone the drone doing the harvesting
     */
    public void harvest(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        world.destroyBlock(pos, true);
    }
    
    /**
     * Should harvest the block (drop items), and replant the plant if applicable. For example, for crops it should reset the growth stage to 0.
     * @param world the world
     * @param chunkCache Use preferably methods from this cache as it's generally quicker than accessing via 'world'. 
     * The cache has access to the chunks that are accessed by the Drone current program, so as long as only the y pos is varied of the
     * supplied pos, you are good. If not, use 'world'.
     * @param pos the block's position
     * @param state the blockstate at the position
     * @param drone the drone doing the harvesting
     * @return true if the replanting succeeded (and the hoe the Drone carries needs to be damaged). If nothing needed to be replanted return false.
     */
    public boolean harvestAndReplant(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone) {
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
    public abstract boolean canHarvest(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone);
    
    /**
     * Should add the items the connected item filters in the Harvest puzzle piece in the Programmer can use to determine if a block should be harvested.
     * Called after {@link HarvestHandler#canHarvest(World, IBlockReader, BlockPos, BlockState, IDrone)}, when that method returns true
     * @param world the world
     * @param chunkCache Use preferably methods from this cache as it's generally quicker than accessing via 'world'. 
     * The cache has access to the chunks that are accessed by the Drone current program, so as long as only the y pos is varied of the
     * supplied pos, you are good. If not, use 'world'.
     * @param pos the blockpos
     * @param state the blockstate
     * @param drone the drone
     */
    public List<ItemStack> addFilterItems(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone){
        return world instanceof ServerWorld ? Block.getDrops(state, (ServerWorld) world, pos, world.getBlockEntity(pos)) : Collections.emptyList();
    }

    /**
     * A simple harvest handler which just compares against a list of blocks, without checking any blockstate properties.
     */
    public static class SimpleHarvestHandler extends HarvestHandler {
        private final Set<Block> blocks;

        public SimpleHarvestHandler(Block... blocks) {
            this.blocks = Sets.newHashSet(blocks);
        }

        @Override
        public boolean canHarvest(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone) {
            return blocks.contains(state.getBlock());
        }
    }
}
