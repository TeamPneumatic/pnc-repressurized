package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.common.thirdparty.RadiationSourceCheck;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.lib.radiation.capability.IRadiationShielding;
import mekanism.common.registries.MekanismDamageSource;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class MekanismIntegration {
    @CapabilityInject(IHeatHandler.class)
    public static final Capability<IHeatHandler> CAPABILITY_HEAT_HANDLER = null;

    @CapabilityInject(IRadiationShielding.class)
    public static final Capability<IRadiationShielding> CAPABILITY_RADIATION_SHIELDING = null;

    static void mekSetup() {
        // FIXME non-api usage here (ask Mek team to provide an API method?)
        RadiationSourceCheck.INSTANCE.registerRadiationSource(s -> s == MekanismDamageSource.RADIATION);
    }
}
