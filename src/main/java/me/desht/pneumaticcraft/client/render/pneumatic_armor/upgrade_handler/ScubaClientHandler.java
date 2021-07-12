package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.ScubaHandler;

public class ScubaClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<ScubaHandler> {
    public ScubaClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().scubaHandler);
    }
}
