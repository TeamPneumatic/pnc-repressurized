package pneumaticCraft.common.heat.behaviour;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import pneumaticCraft.lib.Names;

public class HeatBehaviourWaterSolidify extends HeatBehaviourLiquidTransition{

    @Override
    public String getId(){
        return Names.MOD_ID + ":waterSolidify";
    }

    @Override
    protected int getMinFluidTemp(){
        return Integer.MIN_VALUE;
    }

    @Override
    protected int getMaxFluidTemp(){
        return 1299;
    }

    @Override
    protected int getMaxExchangedHeat(){
        return 10000;
    }

    @Override
    protected Block getTransitionedSourceBlock(){
        return Blocks.ice;
    }

    @Override
    protected Block getTransitionedFlowingBlock(){
        return Blocks.snow;
    }

    @Override
    protected boolean transitionOnTooMuchExtraction(){
        return true;
    }
}
