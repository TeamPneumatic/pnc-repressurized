package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IKeybindingButton;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.KickClientHandler;

import java.util.Optional;

public class KickOptions extends IOptionPage.SimpleToggleableOptions<KickClientHandler> {
    private IKeybindingButton changeKeybindingButton;

    public KickOptions(IGuiScreen screen, KickClientHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        changeKeybindingButton = PneumaticHelmetRegistry.getInstance().makeKeybindingButton(128, KeyHandler.getInstance().keybindKick);
        gui.addWidget(changeKeybindingButton.asWidget());
    }

    @Override
    public Optional<IKeybindingButton> getKeybindingButton() {
        return Optional.of(changeKeybindingButton);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

}
