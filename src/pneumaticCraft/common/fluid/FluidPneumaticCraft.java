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
        return getBlock().getIcon(0, 0);
    }

    @Override
    public IIcon getFlowingIcon(){
        return getBlock().getIcon(1, 0);
    }
}
