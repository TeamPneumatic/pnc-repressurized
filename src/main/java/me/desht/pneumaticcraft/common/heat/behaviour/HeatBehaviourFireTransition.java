package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.init.Blocks;

public class HeatBehaviourFireTransition extends HeatBehaviourTransition {

    @Override
    public boolean isApplicable() {
        return super.isApplicable() && getBlockState().getBlock() == Blocks.FIRE;
    }

    @Override
    protected int getMaxExchangedHeat() {
        return 1000;
    }

    @Override
    protected boolean transitionOnTooMuchExtraction() {
        return true;
    }

    @Override
    protected void transformBlock() {
        getWorld().setBlockToAir(getPos());
    }

    @Override
    public String getId() {
        return Names.MOD_ID + ":fire";
    }

}
