package me.desht.pneumaticcraft.common.harvesting;

import java.util.List;
import java.util.function.Predicate;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.IHarvestHandler;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class HarvestHandlerTree implements IHarvestHandler{

    private final Predicate<IBlockState> blockChecker; //Either for logs or leaves
    private final Predicate<ItemStack> isSapling;
    private final IBlockState saplingState;
    
    public HarvestHandlerTree(Predicate<IBlockState> blockChecker, Predicate<ItemStack> isSapling, IBlockState saplingState){
        this.blockChecker = blockChecker;
        this.isSapling = isSapling;
        this.saplingState = saplingState;
    }
    
    @Override
    public boolean canHarvest(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, IDrone drone){
        return blockChecker.test(state);
    }
    
    @Override
    public boolean harvestAndReplant(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, IDrone drone){
        harvest(world, chunkCache, pos, state, drone);
        if(saplingState.getBlock().canPlaceBlockAt(world, pos)){ //If on dirt (probably)
            int saplingPickRange = 8;
            List<EntityItem> saplingItems = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).grow(saplingPickRange, saplingPickRange, saplingPickRange), entityItem -> isSapling.test(entityItem.getItem()));
            if(!saplingItems.isEmpty()){
                saplingItems.get(0).getItem().shrink(1);//Use a sapling
                world.setBlockState(pos, saplingState); //And plant it.
                return true;
            }
        }
        return false;
    }
}
