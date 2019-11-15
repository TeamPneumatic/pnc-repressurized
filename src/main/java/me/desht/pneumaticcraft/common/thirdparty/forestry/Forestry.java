package me.desht.pneumaticcraft.common.thirdparty.forestry;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class Forestry implements IThirdParty {
    @Override
    public void init() {
        // TODO 1.14 verify fluid names
        IThirdParty.registerFuel("forestry:biomass", 500000);
        IThirdParty.registerFuel("forestry:bio_ethanol", 500000);
    }
}
