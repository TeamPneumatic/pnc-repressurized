/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.KickOptions;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPneumaticKick;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.KickHandler;
import net.minecraft.client.KeyMapping;

import java.util.Optional;

public class KickClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<KickHandler> {
    public KickClientHandler() {
        super(CommonUpgradeHandlers.kickHandler);
    }

    @Override
    public Optional<KeyMapping> getTriggerKeyBinding() {
        return Optional.of(KeyHandler.getInstance().keybindKick);
    }

    @Override
    public void onTriggered(ICommonArmorHandler armorHandler) {
        if (armorHandler.upgradeUsable(CommonUpgradeHandlers.kickHandler, false)) {
            NetworkHandler.sendToServer(PacketPneumaticKick.INSTANCE);
        }
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
