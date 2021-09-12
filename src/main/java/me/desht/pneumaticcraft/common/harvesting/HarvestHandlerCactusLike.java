package me.desht.pneumaticcraft.common.harvesting;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class HarvestHandlerCactusLike extends HarvestHandler {

    private final Predicate<BlockState> blockChecker;
    
    public HarvestHandlerCactusLike(Predicate<BlockState> blockChecker){
        this.blockChecker = blockChecker;
    }
    
    @Override
    public boolean canHarvest(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone){
        if(blockChecker.test(state)){
            BlockState stateBelow = chunkCache.getBlockState(pos.relative(Direction.DOWN));
            return blockChecker.test(stateBelow);
        }
        return false;
    }
    
}
