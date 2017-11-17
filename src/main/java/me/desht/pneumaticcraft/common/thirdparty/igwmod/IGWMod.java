package me.desht.pneumaticcraft.common.thirdparty.igwmod;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class IGWMod implements IThirdParty {

    @Override
    public void clientPreInit() {
        FMLInterModComms.sendMessage("igwmod", "me.desht.pneumaticcraft.common.thirdparty.igwmod.IGWHandler", "init");
    }

}
