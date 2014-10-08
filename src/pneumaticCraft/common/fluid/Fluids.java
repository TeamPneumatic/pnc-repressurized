package pneumaticCraft.common.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.common.block.Blockss;

public class Fluids{
    public static Fluid EtchAcid;
    public static Fluid plastic;

    public static void initFluids(){
        plastic = new FluidPlastic("plastic");
        EtchAcid = new Fluid("EtchAcid"){
            @Override
            public int getColor(){
                return Blockss.etchingAcid.colorMultiplier(null, 0, 0, 0);
            }
        };

        FluidRegistry.registerFluid(EtchAcid);
        FluidRegistry.registerFluid(plastic);
    }
}
