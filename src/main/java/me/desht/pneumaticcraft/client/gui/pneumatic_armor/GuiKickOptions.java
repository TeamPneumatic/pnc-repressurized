package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import net.minecraft.client.gui.GuiButton;

public class GuiKickOptions implements IOptionPage {
    private KeybindingButton changeKeybindingButton;

    public GuiKickOptions() {
    }

    @Override
    public String getPageName() {
        return "Pneumatic Kick";
    }

    @Override
    public void initGui(IGuiScreen gui) {
        changeKeybindingButton = new KeybindingButton(10, 30, 128, 150, 20, "Change Kick Key...", KeyHandler.getInstance().keybindKick);
        gui.getButtonList().add(changeKeybindingButton);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) {
            changeKeybindingButton.toggleKeybindMode();
        }
    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks) {

    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {

    }

    @Override
    public void keyTyped(char ch, int key) {
        if (changeKeybindingButton != null) changeKeybindingButton.receiveKey(key);
    }

    @Override
    public void mouseClicked(int x, int y, int button) {

    }

    @Override
    public void handleMouseInput() {

    }

    @Override
    public boolean canBeTurnedOff() {
        return false;
    }

    @Override
    public boolean displaySettingsText() {
        return false;
    }
}
