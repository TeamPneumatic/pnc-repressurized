package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import cofh.api.util.ThermalExpansionHelper;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class CoFHCore implements IThirdParty {
    @Override
    public void preInit() {

    }

    @Override
    public void init() {
        // gasoline is equivalent to Thermal Foundation refined fuel @ 2,000,000
        // "oil" gets added by CoFH so no need to do it here
        ThermalExpansionHelper.addCompressionFuel(Fluids.DIESEL.getName(), 1000000);
        ThermalExpansionHelper.addCompressionFuel(Fluids.KEROSENE.getName(), 1500000);
        ThermalExpansionHelper.addCompressionFuel(Fluids.GASOLINE.getName(), 2000000);
        ThermalExpansionHelper.addCompressionFuel(Fluids.LPG.getName(), 2500000);
        PneumaticCraftRepressurized.logger.info("Added PneumaticCraft: Repressurized fuels to CoFH Compression Dynamo");

        registerCoFHfuel("creosote", 75000);
        registerCoFHfuel("coal", 300000);
        registerCoFHfuel("tree_oil", 750000);
        registerCoFHfuel("refined_oil", 937500);
        registerCoFHfuel("refined_fuel", 1500000);

        Fluid crudeOil = FluidRegistry.getFluid("crude_oil");
        if (crudeOil != null) {
            PneumaticCraftAPIHandler.getInstance().registerRefineryInput(crudeOil);
            PneumaticCraftRepressurized.logger.info("Added CoFH Crude Oil as a Refinery input");
        }
    }

    private void registerCoFHfuel(String fuelName, int mLPerBucket) {
        Fluid f = FluidRegistry.getFluid(fuelName);
        if (f != null) {
            PneumaticCraftAPIHandler.getInstance().registerFuel(f, mLPerBucket);
            PneumaticCraftRepressurized.logger.info("Added CoFH fuel '" + fuelName + "'");
        } else {
            PneumaticCraftRepressurized.logger.warn("Can't find CoFH fuel: " + fuelName);
        }
    }

    @Override
    public void postInit() {
    }

    @Override
    public void clientSide() {

    }

    @Override
    public void clientInit() {

    }
}
