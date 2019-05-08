package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.MainHelmetHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiHelmetMainOptions implements IOptionPage {

    private final MainHelmetHandler renderHandler;
    private KeybindingButton changeKeybindingButton;

    public GuiHelmetMainOptions(MainHelmetHandler renderHandler) {
        this.renderHandler = renderHandler;
    }

    @Override
    public String getPageName() {
        return "General";
    }

    @Override
    public void initGui(IGuiScreen gui) {
        gui.getButtonList().add(new GuiButton(10, 30, 128, 150, 20, "Move Pressure Stat Screen..."));
        gui.getButtonList().add(new GuiButton(11, 30, 150, 150, 20, "Move Message Screen..."));
        changeKeybindingButton = new KeybindingButton(12, 30, 172, 150, 20, "Change open menu key...", KeyHandler.getInstance().keybindOpenOptions);
        gui.getButtonList().add(changeKeybindingButton);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 10:
                Minecraft.getMinecraft().displayGuiScreen(new GuiMoveStat(renderHandler));
                break;
            case 11:
                renderHandler.testMessageStat = new GuiAnimatedStat(null, "Test Message, keep in mind messages can be long!",
                        renderHandler.messagesStatX, renderHandler.messagesStatY, 0x7000AA00,
                        null, renderHandler.messagesStatLeftSided);
                renderHandler.testMessageStat.openWindow();
                Minecraft.getMinecraft().displayGuiScreen(new GuiMoveStat(renderHandler, renderHandler.testMessageStat));
                break;
            case 12:
                changeKeybindingButton.toggleKeybindMode();
                break;
        }
    }

    @Override
    public void keyTyped(char ch, int key) {
        if (changeKeybindingButton != null) {
            changeKeybindingButton.receiveKey(key);
        }
    }

    @Override
    public void mouseClicked(int x, int y, int button) {
    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks) {
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
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
        return true;
    }
}
