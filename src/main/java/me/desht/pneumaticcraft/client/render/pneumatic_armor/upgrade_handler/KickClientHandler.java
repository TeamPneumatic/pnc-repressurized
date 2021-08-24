package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.KickOptions;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPneumaticKick;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.KickHandler;
import net.minecraft.client.settings.KeyBinding;

import java.util.Optional;

public class KickClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<KickHandler> {
    public KickClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().kickHandler);
    }

    @Override
    public Optional<KeyBinding> getTriggerKeyBinding() {
        return Optional.of(KeyHandler.getInstance().keybindKick);
    }

    @Override
    public void onTriggered(ICommonArmorHandler armorHandler) {
        if (armorHandler.upgradeUsable(ArmorUpgradeRegistry.getInstance().kickHandler, false)) {
            NetworkHandler.sendToServer(new PacketPneumaticKick());
        }
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new KickOptions(screen, this);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }
}
