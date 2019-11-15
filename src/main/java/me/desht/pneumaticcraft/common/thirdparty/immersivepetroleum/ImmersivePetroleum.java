package me.desht.pneumaticcraft.common.thirdparty.immersivepetroleum;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class ImmersivePetroleum implements IThirdParty {
    @Override
    public void init() {
        // Don't need to register IP "diesel" - it's native to PneumaticCraft
        // TODO 1.14 verify fluid name
        IThirdParty.registerFuel("immersivepetroleum:gasoline", 1500000);
    }
}
