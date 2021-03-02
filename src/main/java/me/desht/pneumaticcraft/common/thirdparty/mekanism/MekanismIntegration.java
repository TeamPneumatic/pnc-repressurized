package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import mekanism.api.heat.IHeatHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class MekanismIntegration {
    @CapabilityInject(IHeatHandler.class)
    public static final Capability<IHeatHandler> CAPABILITY_HEAT_HANDLER = null;

    static void mekSetup() {

    }
}
