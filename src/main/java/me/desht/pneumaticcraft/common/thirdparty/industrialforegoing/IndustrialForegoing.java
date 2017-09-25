package me.desht.pneumaticcraft.common.thirdparty.industrialforegoing;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraftforge.fluids.FluidRegistry;

public class IndustrialForegoing implements IThirdParty {
    @Override
    public void preInit() {

    }

    @Override
    public void init() {
        PneumaticRegistry.getInstance().registerXPLiquid(FluidRegistry.getFluid("essence"), 20);
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
