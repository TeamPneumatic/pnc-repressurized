package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class TheOneProbe implements IThirdParty {
    @Override
    public void preInit() {
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "me.desht.pneumaticcraft.common.thirdparty.theoneprobe.TOPCallback");
    }
}
