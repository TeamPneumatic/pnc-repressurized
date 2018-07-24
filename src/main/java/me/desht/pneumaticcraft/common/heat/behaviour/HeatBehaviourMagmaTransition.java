package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.init.Blocks;

public class HeatBehaviourMagmaTransition extends HeatBehaviourTransition {
    @Override
    public boolean isApplicable() {
        return super.isApplicable() && getBlockState().getBlock() == Blocks.MAGMA;
    }

    @Override
    protected int getMaxExchangedHeat() {
        return 2000;
    }

    @Override
    protected boolean transitionOnTooMuchExtraction() {
        return true;
    }

    @Override
    protected void transformBlock() {
        getWorld().setBlockState(getPos(), Blocks.STONE.getDefaultState());
    }

    @Override
    public String getId() {
        return Names.MOD_ID + ":magma";
    }
}
