package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public abstract class HeatBehaviourLiquid extends HeatBehaviour {
    public Fluid getFluid() {
        Fluid fluid = FluidRegistry.lookupFluidForBlock(getBlockState().getBlock());
        if (fluid != null) return fluid;
        else if (getBlockState().getBlock() == Blocks.FLOWING_LAVA) return FluidRegistry.LAVA;
        else if (getBlockState().getBlock() == Blocks.FLOWING_WATER) return FluidRegistry.WATER;
        return null;
    }
}
