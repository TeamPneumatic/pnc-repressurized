package me.desht.pneumaticcraft.common.thirdparty.igwmod;

import igwmod.api.WikiRegistry;
import me.desht.pneumaticcraft.common.thirdparty.IDocsProvider;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class IGWMod implements IThirdParty, IDocsProvider {

    @Override
    public void clientPreInit() {
        FMLInterModComms.sendMessage("igwmod", "me.desht.pneumaticcraft.common.thirdparty.igwmod.IGWHandler", "init");
        ThirdPartyManager.instance().docsProvider = this;
    }

    @Override
    public void showWidgetDocs(String path) {
        WikiRegistry.getWikiHooks().showWikiGui("pneumaticcraft:progwidget/" + path);
    }
}
