package me.desht.pneumaticcraft.common.harvesting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import me.desht.pneumaticcraft.api.harvesting.IHarvestHandler;
import me.desht.pneumaticcraft.api.harvesting.IHarvestRegistry;
import me.desht.pneumaticcraft.common.block.Blockss;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import org.apache.commons.lang3.Validate;


public class HarvestRegistry implements IHarvestRegistry {
    private static final HarvestRegistry INSTANCE = new HarvestRegistry();
    
    private List<IHarvestHandler> harvestHandlers = new ArrayList<IHarvestHandler>();

    public static HarvestRegistry getInstance() {
        return INSTANCE;
    }

    public void init() {
        //Crops, harvest when fully grown
        registerHarvestHandler((w, c, p, state) -> state.getBlock() instanceof BlockCrops && 
                                                   ((BlockCrops)state.getBlock()).isMaxAge(state));
        registerHarvestHandler((w, c, p, state) -> state.getBlock() == Blocks.NETHER_WART && 
                                                   state.getValue(BlockNetherWart.AGE) >= 3);
        registerHarvestHandler((w, c, p, state) -> state.getBlock() == Blocks.COCOA && 
                                                   state.getValue(BlockNetherWart.AGE) >= 2);

        //Cactus like, harvest when a block below.
        registerHarvestHandlerCactuslike(state -> state.getBlock() == Blocks.CACTUS);
        registerHarvestHandlerCactuslike(state -> state.getBlock() == Blocks.REEDS);
        
        //Melons/Pumpkins, just harvest the block when found
        registerHarvestHandler((w, c, p, state) -> state.getBlock() == Blocks.PUMPKIN ||
                                                   state.getBlock() == Blocks.MELON_BLOCK);

    }
    
    public List<IHarvestHandler> getHarvestHandlers(){
        return harvestHandlers;
    }
    
    @Override
    public void registerHarvestHandler(IHarvestHandler harvestHandler){
        Validate.notNull(harvestHandler);
        harvestHandlers.add(harvestHandler);
    }
    
    @Override
    public void registerHarvestHandlerCactuslike(Predicate<IBlockState> blockChecker){
        Validate.notNull(blockChecker);
        registerHarvestHandler(new HarvestHandlerCactusLike(blockChecker));
    }
}
