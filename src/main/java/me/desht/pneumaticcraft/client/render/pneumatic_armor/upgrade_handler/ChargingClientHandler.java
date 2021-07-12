package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.ChargingHandler;

public class ChargingClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<ChargingHandler> {
    public ChargingClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().chargingHandler);
    }
}
