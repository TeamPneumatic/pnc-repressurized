package me.desht.pneumaticcraft.common.harvesting;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class HarvestHandlerCropLike extends HarvestHandlerAbstractCrop{

    private final IntegerProperty ageProperty;
    private final int minAge, maxAge;
    private final Predicate<ItemStack> isSeed;
    
    public HarvestHandlerCropLike(Predicate<BlockState> blockChecker, IntegerProperty ageProperty, Predicate<ItemStack> isSeed){
        super(blockChecker);
        this.ageProperty = ageProperty;
        this.isSeed = isSeed;
        minAge = ageProperty.getAllowedValues().stream().mapToInt(Integer::intValue).min().getAsInt();
        maxAge = ageProperty.getAllowedValues().stream().mapToInt(Integer::intValue).max().getAsInt();
    }
    
    @Override
    protected boolean isSeed(World world, BlockPos pos, BlockState state, ItemStack stack){
        return isSeed.test(stack);
    }
    
    @Override
    protected boolean isMaxAge(BlockState state){
        return state.get(ageProperty) == maxAge;
    }
    
    @Override
    protected BlockState withMinAge(BlockState state){
        return state.with(ageProperty, minAge);
    }
}
