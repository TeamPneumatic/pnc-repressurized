package pneumaticCraft.common.thirdparty.openblocks;

import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.thirdparty.IThirdParty;

public class OpenBlocks implements IThirdParty{

    @Override
    public void preInit(){}

    @Override
    public void init(){
        PneumaticRegistry.getInstance().registerXPLiquid(FluidRegistry.getFluid("xpjuice"), 20);
    }

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){}

    @Override
    public void clientInit(){}

}
