package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
//import toughasnails.api.temperature.TemperatureHelper;

public class ToughAsNails implements IThirdParty {
    @Override
    public void init() {
        NetworkHandler.registerMessage(PacketPlayerTemperatureDelta.class,
                PacketPlayerTemperatureDelta::toBytes, PacketPlayerTemperatureDelta::new, PacketPlayerTemperatureDelta::handle);
    }

    @Override
    public void postInit() {
//        TemperatureHelper.registerTemperatureModifier(new TANModifierPNCBlock());
//        TemperatureHelper.registerTemperatureModifier(new TANModifierAirConditioning());
    }
}
