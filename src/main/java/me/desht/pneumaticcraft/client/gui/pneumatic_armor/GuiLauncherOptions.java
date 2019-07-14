package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;

public class GuiLauncherOptions implements IOptionPage {
    private KeybindingButton changeKeybindingButton;

    @Override
    public String getPageName() {
        return "Item Launcher";
    }

    @Override
    public void initGui(IGuiScreen gui) {
        changeKeybindingButton = new KeybindingButton(30, 128, 150, 20, "Change Launch Key...", KeyHandler.getInstance().keybindLauncher, b -> changeKeybindingButton.toggleKeybindMode());
        gui.getWidgetList().add(changeKeybindingButton);
    }

    public void renderPre(int x, int y, float partialTicks) {

    }

    public void renderPost(int x, int y, float partialTicks) {

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (changeKeybindingButton != null) {
            changeKeybindingButton.receiveKey(keyCode);
            return true;
        }
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
    public boolean canBeTurnedOff() {
        return false;
    }

    @Override
    public boolean displaySettingsHeader() {
        return false;
    }
}
