package pneumaticCraft.common.fluid;

import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.common.block.BlockFluidPneumaticCraft;

public class FluidPneumaticCraft extends Fluid{

    public FluidPneumaticCraft(String fluidName){
        this(fluidName, true);
    }

    public FluidPneumaticCraft(String fluidName, boolean registerBlock){
        super(fluidName);
        FluidRegistry.registerFluid(this);
        if(registerBlock) setBlock(new BlockFluidPneumaticCraft(this));
    }

    @Override
    public IIcon getStillIcon(){
        return ((BlockFluidPneumaticCraft)getBlock()).stillIcon;
    }

    @Override
    public IIcon getFlowingIcon(){
        return ((BlockFluidPneumaticCraft)getBlock()).flowingIcon;
    }
}
