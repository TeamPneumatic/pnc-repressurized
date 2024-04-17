package me.desht.pneumaticcraft.common.thirdparty.ffs;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.neoforged.bus.api.IEventBus;

public class FTBFilterSystem implements IThirdParty {
    @Override
    public void preInit(IEventBus modBus) {
        modBus.addListener(FFSSetup::registerCaps);
    }
}
