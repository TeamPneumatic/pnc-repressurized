package me.desht.pneumaticcraft.client.gui.pneumaticHelmet;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

/**
 * Suitable for a simple on/off armor option with no other settings or rendering
 */
public class SimpleToggleableOptions implements IOptionPage {
    private final String name;

    public SimpleToggleableOptions(IUpgradeRenderHandler handler) {
        this.name = I18n.format("pneumaticHelmet.upgrade." + handler.getUpgradeName());
    }

    @Override
    public String getPageName() {
        return name;
    }

    @Override
    public void initGui(IGuiScreen gui) {

    }

    @Override
    public void actionPerformed(GuiButton button) {

    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks) {

    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {

    }

    @Override
    public void keyTyped(char ch, int key) {

    }

    @Override
    public void mouseClicked(int x, int y, int button) {

    }

    @Override
    public void handleMouseInput() {

    }

    @Override
    public boolean canBeTurnedOff() {
        return true;
    }

    @Override
    public boolean displaySettingsText() {
        return false;
    }
}
