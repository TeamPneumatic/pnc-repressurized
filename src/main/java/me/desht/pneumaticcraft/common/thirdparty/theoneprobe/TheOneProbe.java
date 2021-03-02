package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraftforge.fml.InterModComms;

public class TheOneProbe implements IThirdParty {
    @Override
    public void init() {
        InterModComms.sendTo(ModIds.TOP, "getTheOneProbe", TOPInit::new);
    }
}
