package me.desht.pneumaticcraft.common.harvesting;

import java.util.Random;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.IHarvestHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class HarvestHandlerLeaves implements IHarvestHandler{

    @Override
    public boolean canHarvest(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, IDrone drone){
        return state.getBlock() == Blocks.LEAVES || state.getBlock() == Blocks.LEAVES2;
    }

    @Override
    public void addFilterItems(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, NonNullList<ItemStack> stacks, IDrone drone){
        Block block = state.getBlock();
        stacks.add(new ItemStack(block.getItemDropped(state, new Random(), 0), 1, block.damageDropped(state)));
    }
}
