package me.desht.pneumaticcraft.common.thirdparty.curios;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class Curios implements IThirdParty {
    public static boolean available = false;

    @Override
    public void preInit() {
        available = true;
    }
}
