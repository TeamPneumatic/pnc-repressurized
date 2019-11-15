package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.MainHelmetHandler;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import net.minecraft.client.Minecraft;

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
        gui.getWidgetList().add(new GuiButtonSpecial(30, 128, 150, 20,
                "Move Pressure Stat Screen...",
                b -> Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(renderHandler, ArmorHUDLayout.LayoutTypes.POWER)))
        );

        gui.getWidgetList().add(new GuiButtonSpecial(30, 150, 150, 20,
                "Move Message Screen...", b -> {
            renderHandler.testMessageStat = new GuiAnimatedStat(null, "Test Message, keep in mind messages can be long!",
                    GuiAnimatedStat.StatIcon.NONE, 0x7000AA00, null, ArmorHUDLayout.INSTANCE.messageStat);
            renderHandler.testMessageStat.openWindow();
            Minecraft.getInstance().displayGuiScreen(
                    new GuiMoveStat(renderHandler, ArmorHUDLayout.LayoutTypes.MESSAGE, renderHandler.testMessageStat));
        }));

        changeKeybindingButton = new KeybindingButton(30, 172, 150, 20,
                "Change open menu key...", KeyHandler.getInstance().keybindOpenOptions,
                b -> changeKeybindingButton.toggleKeybindMode()
        );
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
        return true;
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }
}
