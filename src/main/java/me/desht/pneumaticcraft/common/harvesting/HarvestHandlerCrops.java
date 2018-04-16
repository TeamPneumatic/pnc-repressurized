package me.desht.pneumaticcraft.common.harvesting;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Harvest handler targeted at handling any subclass of BlockCrops.
 * @author MineMaarten
 *
 */
public class HarvestHandlerCrops extends HarvestHandlerAbstractCrop{    
    public HarvestHandlerCrops(){
        super(state -> state.getBlock() instanceof BlockCrops);
    }
    
    @Override
    public boolean isSeed(World world, BlockPos pos, IBlockState state, ItemStack stack){
        ItemStack seed = ((BlockCrops)state.getBlock()).getItem(world, pos, withMinAge(state));
        return seed != null && seed.isItemEqual(stack);
    }

    @Override
    protected boolean isMaxAge(IBlockState state){
        return ((BlockCrops)state.getBlock()).isMaxAge(state);
    }
    
    @Override
    protected IBlockState withMinAge(IBlockState state){
        return ((BlockCrops)state.getBlock()).withAge(0);
    }
}
