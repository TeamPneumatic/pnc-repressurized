package pneumaticCraft.common;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.common.block.Blockss;

public class Fluids{
    public static Fluid EtchAcid;

    public static void initFluids(){
        EtchAcid = new Fluid("EtchAcid"){
            @Override
            public int getColor(){
                return Blockss.etchingAcid.colorMultiplier(null, 0, 0, 0);
            }
        };

        FluidRegistry.registerFluid(EtchAcid);
    }
}
