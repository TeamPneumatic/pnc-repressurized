package me.desht.pneumaticcraft.common.harvesting;

import java.util.function.Predicate;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.IHarvestHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class HarvestHandlerCactusLike implements IHarvestHandler{

    private final Predicate<IBlockState> blockChecker;
    
    public HarvestHandlerCactusLike(Predicate<IBlockState> blockChecker){
        this.blockChecker = blockChecker;
    }
    
    @Override
    public boolean canHarvest(World world, IBlockAccess chunkCache, BlockPos pos, IBlockState state, IDrone drone){
        if(blockChecker.test(state)){
            IBlockState stateBelow = chunkCache.getBlockState(pos.offset(EnumFacing.DOWN));
            return blockChecker.test(stateBelow);
        }
        return false;
    }
    
}
