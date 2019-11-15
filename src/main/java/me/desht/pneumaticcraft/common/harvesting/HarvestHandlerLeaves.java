package me.desht.pneumaticcraft.common.harvesting;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.common.core.ModHarvestHandlers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class HarvestHandlerLeaves extends HarvestHandler {

    @Override
    public boolean canHarvest(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone){
        return state.getBlock() instanceof LeavesBlock;
    }

    @Override
    public List<ItemStack> addFilterItems(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        Block sapling = ModHarvestHandlers.convertTree(state.getBlock(), "leaves", "sapling");
        return Collections.singletonList(new ItemStack(sapling));
    }
}
