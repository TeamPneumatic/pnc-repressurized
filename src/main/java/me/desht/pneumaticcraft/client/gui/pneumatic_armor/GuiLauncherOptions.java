package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.ChestplateLauncherHandler;

public class GuiLauncherOptions extends IOptionPage.SimpleToggleableOptions<ChestplateLauncherHandler> {
    private KeybindingButton changeKeybindingButton;

    public GuiLauncherOptions(IGuiScreen screen, ChestplateLauncherHandler renderHandler) {
        super(screen, renderHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        changeKeybindingButton = new KeybindingButton(30, 128, 150, 20, "Change Launch Key...", KeyHandler.getInstance().keybindLauncher, b -> changeKeybindingButton.toggleKeybindMode());
        gui.addWidget(changeKeybindingButton);
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
    public boolean isToggleable() {
        return false;
    }

}
