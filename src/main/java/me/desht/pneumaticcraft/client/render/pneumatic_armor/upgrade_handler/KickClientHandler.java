package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.KickOptions;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.KickHandler;

public class KickClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<KickHandler> {
    public KickClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().kickHandler);
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new KickOptions(screen, this);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }
}
