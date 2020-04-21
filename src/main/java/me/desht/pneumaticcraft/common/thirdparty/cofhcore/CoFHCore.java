package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid= Names.MOD_ID)
public class CoFHCore implements IThirdParty {

    @Override
    public void init() {
        // gasoline is equivalent to Thermal Foundation refined fuel @ 2,000,000
        // "oil" gets added by CoFH so no need to do it here
        // TODO 1.14
//        ThermalExpansionHelper.addCompressionFuel(Fluids.DIESEL.getName(), 1000000);
//        ThermalExpansionHelper.addCompressionFuel(Fluids.KEROSENE.getName(), 1500000);
//        ThermalExpansionHelper.addCompressionFuel(Fluids.GASOLINE.getName(), 2000000);
//        ThermalExpansionHelper.addCompressionFuel(Fluids.LPG.getName(), 2500000);
        Log.info("(not implemented) Added PneumaticCraft: Repressurized fuels to CoFH Compression Dynamo");

        // TODO 1.14 verify these will be the right fluid names
        IThirdParty.registerFuel("thermalfoundation:creosote", 75000);
        IThirdParty.registerFuel("thermalfoundation:coal", 300000);
        IThirdParty.registerFuel("thermalfoundation:tree_oil", 750000);
        IThirdParty.registerFuel("thermalfoundation:refined_oil", 937500);
        IThirdParty.registerFuel("thermalfoundation:refined_fuel", 1500000);
    }

//    @SubscribeEvent
//    public static void register(RegisterMachineRecipesEvent evt) {
//        // TODO 1.14 do this via tags (i.e. add CoFH oil to "pneumaticcraft:oil" fluid tag)
////        Fluid crudeOil = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("thermalfoundation:crude_oil"));
////        if (crudeOil != null && crudeOil != Fluids.EMPTY) {
////            ModRefineryRecipes.addDefaultRefiningRecipe(evt, crudeOil);
////            Log.info("Added CoFH Crude Oil as a Refinery input");
////        }
//    }
}
