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

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorMainScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Special "empty" options page for the case where there are no upgrade screens at all.
 * Should not normally happen since core components page should always be visible, but
 * just to be safe...
 */
public class NullOptions implements IOptionPage {
    private static NullOptions instance = null;
    private final IGuiScreen screen;

    public NullOptions(IGuiScreen screen) {
        this.screen = screen;
    }

    public static NullOptions get(ArmorMainScreen guiArmorMainScreen) {
        if (instance == null) {
            instance = new NullOptions(guiArmorMainScreen);
        }
        return instance;
    }

    @Override
    public IGuiScreen getGuiScreen() {
        return screen;
    }

    @Override
    public MutableComponent getPageName() {
        return xlate("pneumaticcraft.armor.gui.misc.noUpgrades");
    }

    @Override
    public void populateGui(IGuiScreen gui) {
    }

    @Override
    public void renderPre(GuiGraphics graphics, int x, int y, float partialTicks) {
    }

    @Override
    public void renderPost(GuiGraphics graphics, int x, int y, float partialTicks) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dir) {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public boolean displaySettingsHeader() {
        return false;
    }

    @Override
    public IArmorUpgradeClientHandler<?> getClientUpgradeHandler() {
        return null;
    }
}
