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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiMoveStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.AirConClientHandler;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import net.minecraft.client.Minecraft;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AirConditionerOptions extends IOptionPage.SimpleOptionPage<AirConClientHandler> {

    public AirConditionerOptions(IGuiScreen screen, AirConClientHandler airConUpgradeHandler) {
        super(screen, airConUpgradeHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        super.populateGui(gui);

        gui.addWidget(new WidgetButtonExtended(30, 128, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"), b -> {
            Minecraft.getInstance().player.closeContainer();
            Minecraft.getInstance().setScreen(new GuiMoveStat(getClientUpgradeHandler(), ArmorHUDLayout.LayoutType.AIR_CON));
        }));
    }
}
