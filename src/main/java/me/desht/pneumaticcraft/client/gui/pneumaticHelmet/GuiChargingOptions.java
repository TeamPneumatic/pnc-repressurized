package me.desht.pneumaticcraft.client.gui.pneumaticHelmet;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.ChargingUpgradeRenderHandler;
import net.minecraft.client.gui.GuiButton;

public class GuiChargingOptions implements IOptionPage {
    private final ChargingUpgradeRenderHandler handler;

    public GuiChargingOptions(ChargingUpgradeRenderHandler handler) {
        this.handler = handler;
    }

    @Override
    public String getPageName() {
        return "Auto-Charger";
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
