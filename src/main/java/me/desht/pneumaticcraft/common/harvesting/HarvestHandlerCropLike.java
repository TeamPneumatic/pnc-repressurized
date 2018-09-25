package me.desht.pneumaticcraft.common.harvesting;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class HarvestHandlerCropLike extends HarvestHandlerAbstractCrop{

    private final PropertyInteger ageProperty;
    private final int minAge, maxAge;
    private final Predicate<ItemStack> isSeed;
    
    public HarvestHandlerCropLike(Predicate<IBlockState> blockChecker, PropertyInteger ageProperty, Predicate<ItemStack> isSeed){
        super(blockChecker);
        this.ageProperty = ageProperty;
        this.isSeed = isSeed;
        minAge = ageProperty.getAllowedValues().stream().mapToInt(Integer::intValue).min().getAsInt();
        maxAge = ageProperty.getAllowedValues().stream().mapToInt(Integer::intValue).max().getAsInt();
    }
    
    @Override
    protected boolean isSeed(World world, BlockPos pos, IBlockState state, ItemStack stack){
        return isSeed.test(stack);
    }
    
    @Override
    protected boolean isMaxAge(IBlockState state){
        return state.getValue(ageProperty) == maxAge;
    }
    
    @Override
    protected IBlockState withMinAge(IBlockState state){
        return state.withProperty(ageProperty, minAge);
    }
}
