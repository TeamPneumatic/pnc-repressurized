package me.desht.pneumaticcraft.common.thirdparty.waila;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class Waila implements IThirdParty {
    public static boolean available = true;

    @Override
    public void init() {
        // don't need to do much here; Waila init is handled implicitly in WailaRegistration
        available = true;
    }
}
