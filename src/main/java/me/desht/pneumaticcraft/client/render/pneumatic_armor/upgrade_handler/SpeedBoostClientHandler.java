package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.SpeedBoostOptions;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.SpeedBoostHandler;

public class SpeedBoostClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<SpeedBoostHandler> {
    public SpeedBoostClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().runSpeedHandler);
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new SpeedBoostOptions(screen, this);
    }
}
