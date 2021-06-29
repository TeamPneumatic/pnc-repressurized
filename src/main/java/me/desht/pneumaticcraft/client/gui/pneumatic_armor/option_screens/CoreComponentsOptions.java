package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IKeybindingButton;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiArmorColors;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiMoveStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.CoreComponentsClientHandler;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class CoreComponentsOptions extends IOptionPage.SimpleToggleableOptions<CoreComponentsClientHandler> {
    private IKeybindingButton changeKeybindingButton;

    public CoreComponentsOptions(IGuiScreen screen, CoreComponentsClientHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, 128, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.movePressureScreen"),
                b -> Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getClientUpgradeHandler(), ArmorHUDLayout.LayoutType.POWER)))
        );

        gui.addWidget(new WidgetButtonExtended(30, 150, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.moveMessageScreen"), b -> {
            getClientUpgradeHandler().testMessageStat = new WidgetAnimatedStat(null, new StringTextComponent("Test Message, keep in mind messages can be long!"),
                    WidgetAnimatedStat.StatIcon.NONE, HUDHandler.getInstance().getStatOverlayColor(), null, ArmorHUDLayout.INSTANCE.messageStat);
            getClientUpgradeHandler().testMessageStat.openStat();
            Minecraft.getInstance().displayGuiScreen(
                    new GuiMoveStat(getClientUpgradeHandler(), ArmorHUDLayout.LayoutType.MESSAGE, getClientUpgradeHandler().testMessageStat));
        }));

        gui.addWidget(new WidgetButtonExtended(30, 194, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.colors"), b -> Minecraft.getInstance().displayGuiScreen(new GuiArmorColors())));

        gui.addWidget(new WidgetCheckBox(5, 45, 0xFFFFFFFF, xlate("pneumaticcraft.armor.gui.misc.showPressureNumerically"), b -> {
            getClientUpgradeHandler().setShowPressureNumerically(b.checked);
            getClientUpgradeHandler().saveToConfig();
        }).setChecked(getClientUpgradeHandler().shouldShowPressureNumerically()));

        changeKeybindingButton = PneumaticHelmetRegistry.getInstance().makeKeybindingButton(172, KeyHandler.getInstance().keybindOpenOptions);
        gui.addWidget(changeKeybindingButton.asWidget());
    }

    @Override
    public Optional<IKeybindingButton> getKeybindingButton() {
        return Optional.of(changeKeybindingButton);
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }
}
