package me.desht.pneumaticcraft.common.harvesting;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.IHarvestHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public abstract class HarvestHandlerAbstractCrop implements IHarvestHandler{

    private final Predicate<IBlockState> blockChecker;
    
    public HarvestHandlerAbstractCrop(Predicate<IBlockState> blockChecker){
        this.blockChecker = blockChecker;
    }
    
    @Override
    public boolean canHarvest(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, IDrone drone){
        return blockChecker.test(state) && isMaxAge(state);
    }
    
    @Override
    public boolean harvestAndReplant(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, IDrone drone){
        harvest(world, chunkCache, pos, state, drone);
        List<EntityItem> seedItems = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos), entityItem -> isSeed(world, pos, state, entityItem.getItem()));
        if(!seedItems.isEmpty()){
            seedItems.get(0).getItem().shrink(1);//Use a seed
            world.setBlockState(pos, withMinAge(state)); //And plant it.
            return true;
        }else{
            return false;
        }
    }
    
    protected abstract boolean isSeed(World world, BlockPos pos, IBlockState state, ItemStack stack);
    
    protected abstract boolean isMaxAge(IBlockState state);
    
    protected abstract IBlockState withMinAge(IBlockState state);
}
