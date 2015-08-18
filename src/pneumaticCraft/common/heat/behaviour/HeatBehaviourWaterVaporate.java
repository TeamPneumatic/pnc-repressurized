package pneumaticCraft.common.heat.behaviour;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import pneumaticCraft.lib.Names;

public class HeatBehaviourWaterVaporate extends HeatBehaviourLiquidTransition{

    @Override
    public String getId(){
        return Names.MOD_ID + ":waterVaporate";
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
        return Blocks.stone;
    }

    @Override
    protected Block getTransitionedFlowingBlock(){
        return Blocks.air;
    }

    @Override
    protected boolean transitionOnTooMuchExtraction(){
        return false;
    }
}
