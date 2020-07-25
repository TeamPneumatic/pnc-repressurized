package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.KickUpgradeHandler;
import net.minecraft.util.text.StringTextComponent;

public class GuiKickOptions extends IOptionPage.SimpleToggleableOptions<KickUpgradeHandler> {
    private KeybindingButton changeKeybindingButton;

    public GuiKickOptions(IGuiScreen screen, KickUpgradeHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public String getPageName() {
        return "Pneumatic Kick";
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        changeKeybindingButton = new KeybindingButton(30, 128, 150, 20,
                new StringTextComponent("Change Kick Key..."), KeyHandler.getInstance().keybindKick, b -> changeKeybindingButton.toggleKeybindMode());
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
