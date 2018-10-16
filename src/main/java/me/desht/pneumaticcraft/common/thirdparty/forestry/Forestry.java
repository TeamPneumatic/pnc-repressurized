package me.desht.pneumaticcraft.common.thirdparty.forestry;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class Forestry implements IThirdParty {
    @Override
    public void init() {
        IThirdParty.registerFuel("biomass", "Forestry", 500000);
        IThirdParty.registerFuel("bio.ethanol", "Forestry", 500000);
    }
}
