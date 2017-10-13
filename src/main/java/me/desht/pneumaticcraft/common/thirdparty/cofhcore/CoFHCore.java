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

        Fluid crudeOil = FluidRegistry.getFluid("crude_oil");
        if (crudeOil != null) {
            PneumaticCraftAPIHandler.getInstance().registerRefineryInput(crudeOil);
            PneumaticCraftRepressurized.logger.info("Added CoFH Crude Oil as a Refinery input");
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
