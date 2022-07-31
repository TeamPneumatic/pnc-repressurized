package me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.FallProtectionHandler;

public class FallProtectionClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<FallProtectionHandler> {
    public FallProtectionClientHandler() {
        super(CommonUpgradeHandlers.fallProtectionHandler);
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }
}
