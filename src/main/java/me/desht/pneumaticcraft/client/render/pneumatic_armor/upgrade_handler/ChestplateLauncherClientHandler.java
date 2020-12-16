package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.ChestplateLauncherOptions;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.client.settings.KeyBinding;

import java.util.Optional;

public class ChestplateLauncherClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler {
    public ChestplateLauncherClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().chestplateLauncherHandler);
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new ChestplateLauncherOptions(screen, this);
    }

    @Override
    public Optional<KeyBinding> getInitialKeyBinding() {
        return Optional.empty();
    }
}
