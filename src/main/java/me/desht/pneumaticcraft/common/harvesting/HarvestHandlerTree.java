package me.desht.pneumaticcraft.common.harvesting;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.IHarvestHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public class HarvestHandlerTree implements IHarvestHandler {

    private final Predicate<BlockState> blockChecker; //Either for logs or leaves
    private final Predicate<ItemStack> isSapling;
    private final BlockState saplingState;
    
    public HarvestHandlerTree(Predicate<BlockState> blockChecker, Predicate<ItemStack> isSapling, BlockState saplingState){
        this.blockChecker = blockChecker;
        this.isSapling = isSapling;
        this.saplingState = saplingState;
    }
    
    @Override
    public boolean canHarvest(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone){
        return blockChecker.test(state);
    }
    
    @Override
    public boolean harvestAndReplant(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone){
        harvest(world, chunkCache, pos, state, drone);
        if (saplingState.isValidPosition(world, pos)) {
            int saplingPickRange = 8;
            List<ItemEntity> saplingItems = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(pos).grow(saplingPickRange, saplingPickRange, saplingPickRange), entityItem -> isSapling.test(entityItem.getItem()));
            if(!saplingItems.isEmpty()){
                saplingItems.get(0).getItem().shrink(1);//Use a sapling
                world.setBlockState(pos, saplingState); //And plant it.
                return true;
            }
        }
        return false;
    }
}
