package me.desht.pneumaticcraft.common.harvesting;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
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
        super(state -> state.getBlock() instanceof CropsBlock);
    }
    
    @Override
    public boolean isSeed(World world, BlockPos pos, BlockState state, ItemStack stack){
        ItemStack seed = ((CropsBlock)state.getBlock()).getItem(world, pos, withMinAge(state));
        return seed.isItemEqual(stack);
    }

    @Override
    protected boolean isMaxAge(BlockState state){
        return ((CropsBlock)state.getBlock()).isMaxAge(state);
    }
    
    @Override
    protected BlockState withMinAge(BlockState state){
        return ((CropsBlock)state.getBlock()).withAge(0);
    }
}
