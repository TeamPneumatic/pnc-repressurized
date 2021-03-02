package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Names.MOD_ID)
public class CoFHCore implements IThirdParty {
    @Override
    public void init() {
        // fuel registration now all done by conditional recipes in ModRecipeProvider
    }
}
