package me.desht.pneumaticcraft.common.thirdparty.forestry;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraftforge.fluids.FluidRegistry;

public class Forestry implements IThirdParty {

    @Override
    public void preInit() {

    }

    @Override
    public void init() {
        PneumaticRegistry.getInstance().registerFuel(FluidRegistry.getFluid("biomass"), 500000);
        PneumaticRegistry.getInstance().registerFuel(FluidRegistry.getFluid("bioethanol"), 500000);
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
