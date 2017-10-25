package me.desht.pneumaticcraft.common.thirdparty.waila;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class Waila implements IThirdParty {
    @Override
    public void init() {
        FMLInterModComms.sendMessage("waila", "register", "me.desht.pneumaticcraft.common.thirdparty.waila.WailaCallback.callback");
    }
}
