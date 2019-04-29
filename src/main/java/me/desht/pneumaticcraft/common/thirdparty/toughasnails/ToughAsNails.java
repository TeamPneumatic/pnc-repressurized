package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import toughasnails.api.temperature.TemperatureHelper;

public class ToughAsNails implements IThirdParty {
    @Override
    public void postInit() {
        TemperatureHelper.registerTemperatureModifier(new PNCBlockModifier());
        TemperatureHelper.registerTemperatureModifier(new AirConditioningModifier());

    }
}
