package pneumaticCraft.common.heat.behaviour;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import pneumaticCraft.lib.Names;

public class HeatBehaviourLava extends HeatBehaviourLiquidTransition{

    @Override
    public String getId(){
        return Names.MOD_ID + ":lava";
    }

    @Override
    protected int getMinFluidTemp(){
        return 1300;
    }

    @Override
    protected int getMaxFluidTemp(){
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getMaxExchangedHeat(){
        return 10000;
    }

    @Override
    protected Block getTransitionedSourceBlock(){
        return Blocks.obsidian;
    }

    @Override
    protected Block getTransitionedFlowingBlock(){
        return Blocks.cobblestone;
    }

    @Override
    protected boolean transitionOnTooMuchExtraction(){
        return true;
    }
}
