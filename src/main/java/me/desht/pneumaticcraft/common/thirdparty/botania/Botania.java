package me.desht.pneumaticcraft.common.thirdparty.botania;

import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class Botania implements IThirdParty {
    @Override
    public void postInit() {
        PneumaticCraftAPIHandler.getInstance().getItemRegistry().registerMagnetSuppressor(new SolegnoliaHandler());

        PlasticBrickDyeHandler.setup();
    }
}
