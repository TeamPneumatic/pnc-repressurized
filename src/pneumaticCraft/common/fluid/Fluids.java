package pneumaticCraft.common.fluid;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.common.block.Blockss;

public class Fluids{
    public static Fluid EtchAcid;
    public static Fluid plastic;
    public static Fluid oil;
    public static Fluid lpg;
    public static Fluid gasoline;
    public static Fluid kerosine;
    public static Fluid diesel;
    public static List<Fluid> textureRegisteredFluids = new ArrayList<Fluid>();
    public static boolean isUsingNativeOil;

    public static void initFluids(){
        plastic = new FluidPlastic("plastic");
        EtchAcid = new Fluid("EtchAcid"){
            @Override
            public int getColor(){
                return Blockss.etchingAcid.colorMultiplier(null, 0, 0, 0);
            }
        };
        lpg = new Fluid("lpg");
        gasoline = new Fluid("gasoline");
        kerosine = new Fluid("kerosine");
        diesel = new Fluid("diesel");

        if(!FluidRegistry.isFluidRegistered("oil")) {
            oil = new Fluid("oil").setDensity(800).setViscosity(10000);
            FluidRegistry.registerFluid(oil);
            isUsingNativeOil = true;
        } else {
            oil = FluidRegistry.getFluid("oil");
        }

        FluidRegistry.registerFluid(EtchAcid);
        FluidRegistry.registerFluid(plastic);
        FluidRegistry.registerFluid(lpg);
        FluidRegistry.registerFluid(gasoline);
        FluidRegistry.registerFluid(kerosine);
        FluidRegistry.registerFluid(diesel);

        textureRegisteredFluids.add(lpg);
        textureRegisteredFluids.add(gasoline);
        textureRegisteredFluids.add(kerosine);
        textureRegisteredFluids.add(diesel);
    }
}
