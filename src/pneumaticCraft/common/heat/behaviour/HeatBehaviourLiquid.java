package pneumaticCraft.common.heat.behaviour;

import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.api.tileentity.HeatBehaviour;

public abstract class HeatBehaviourLiquid extends HeatBehaviour{
    public Fluid getFluid(){
        Fluid fluid = FluidRegistry.lookupFluidForBlock(getBlock());
        if(fluid != null) return fluid;
        else if(getBlock() == Blocks.flowing_lava) return FluidRegistry.LAVA;
        else if(getBlock() == Blocks.flowing_water) return FluidRegistry.WATER;
        return null;
    }
}
