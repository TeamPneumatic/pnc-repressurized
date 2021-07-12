package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.NightVisionHandler;

public class NightVisionClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<NightVisionHandler> {
    public NightVisionClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().nightVisionHandler);
    }
}
