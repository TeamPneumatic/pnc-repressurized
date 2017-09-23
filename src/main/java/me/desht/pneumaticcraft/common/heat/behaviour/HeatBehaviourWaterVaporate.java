package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class HeatBehaviourWaterVaporate extends HeatBehaviourLiquidTransition {

    @Override
    public String getId() {
        return Names.MOD_ID + ":waterVaporate";
    }

    @Override
    protected int getMinFluidTemp() {
        return Integer.MIN_VALUE;
    }

    @Override
    protected int getMaxFluidTemp() {
        return 1299;
    }

    @Override
    protected int getMaxExchangedHeat() {
        return 10000;
    }

    @Override
    protected Block getTransitionedSourceBlock() {
        return Blocks.STONE;
    }

    @Override
    protected Block getTransitionedFlowingBlock() {
        return Blocks.AIR;
    }

    @Override
    protected boolean transitionOnTooMuchExtraction() {
        return false;
    }
}
