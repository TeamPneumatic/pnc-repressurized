package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.MainHelmetHandler;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

public class GuiHelmetMainOptions extends IOptionPage.SimpleToggleableOptions<MainHelmetHandler> {
    private KeybindingButton changeKeybindingButton;

    public GuiHelmetMainOptions(IGuiScreen screen, MainHelmetHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public String getPageName() {
        return "General";
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, 128, 150, 20,
                "Move Pressure Stat Screen...",
                b -> Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getUpgradeHandler(), ArmorHUDLayout.LayoutTypes.POWER)))
        );

        gui.addWidget(new WidgetButtonExtended(30, 150, 150, 20,
                "Move Message Screen...", b -> {
            getUpgradeHandler().testMessageStat = new WidgetAnimatedStat(null, new StringTextComponent("Test Message, keep in mind messages can be long!"),
                    WidgetAnimatedStat.StatIcon.NONE, 0x7000AA00, null, ArmorHUDLayout.INSTANCE.messageStat);
            getUpgradeHandler().testMessageStat.openWindow();
            Minecraft.getInstance().displayGuiScreen(
                    new GuiMoveStat(getUpgradeHandler(), ArmorHUDLayout.LayoutTypes.MESSAGE, getUpgradeHandler().testMessageStat));
        }));

        changeKeybindingButton = new KeybindingButton(30, 172, 150, 20,
                new StringTextComponent("Change open menu key..."), KeyHandler.getInstance().keybindOpenOptions,
                b -> changeKeybindingButton.toggleKeybindMode()
        );
        gui.addWidget(changeKeybindingButton);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return changeKeybindingButton != null && changeKeybindingButton.receiveKey(keyCode);
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }
}
