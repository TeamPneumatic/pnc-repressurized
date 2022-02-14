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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.options;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IKeybindingButton;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.ChestplateLauncherClientHandler;

import java.util.Optional;

public class ChestplateLauncherOptions extends IOptionPage.SimpleOptionPage<ChestplateLauncherClientHandler> {
    private IKeybindingButton changeKeybindingButton;

    public ChestplateLauncherOptions(IGuiScreen screen, ChestplateLauncherClientHandler renderHandler) {
        super(screen, renderHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        changeKeybindingButton = PneumaticHelmetRegistry.getInstance().makeKeybindingButton(128, KeyHandler.getInstance().keybindLauncher);
        gui.addWidget(changeKeybindingButton.asWidget());
    }

    @Override
    public Optional<IKeybindingButton> getKeybindingButton() {
        return Optional.of(changeKeybindingButton);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

}
