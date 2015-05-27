package pneumaticCraft.common.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.common.block.Blockss;

public class Fluids{
    public static Fluid EtchAcid;
    public static Fluid plastic;
    public static Fluid oil;
    public static boolean isUsingNativeOil;

    public static void initFluids(){
        plastic = new FluidPlastic("plastic");
        EtchAcid = new Fluid("EtchAcid"){
            @Override
            public int getColor(){
                return Blockss.etchingAcid.colorMultiplier(null, 0, 0, 0);
            }
        };

        if(!FluidRegistry.isFluidRegistered("oil")) {
            oil = new Fluid("oil").setDensity(800).setViscosity(10000);
            FluidRegistry.registerFluid(oil);
            isUsingNativeOil = true;
        } else {
            oil = FluidRegistry.getFluid("oil");
        }

        FluidRegistry.registerFluid(EtchAcid);
        FluidRegistry.registerFluid(plastic);
    }
}
