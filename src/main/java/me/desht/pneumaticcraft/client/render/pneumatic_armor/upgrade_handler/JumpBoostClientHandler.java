package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.JumpBoostOptions;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.JumpBoostHandler;

public class JumpBoostClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<JumpBoostHandler> {
    public JumpBoostClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().jumpBoostHandler);
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new JumpBoostOptions(screen,this);
    }
}
