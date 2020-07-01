package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.RunSpeedUpgradeHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;

public class GuiSpeedBoostOptions extends GuiSliderOptions<RunSpeedUpgradeHandler> {
    public GuiSpeedBoostOptions(IGuiScreen screen, RunSpeedUpgradeHandler handler) {
        super(screen, handler);
    }

    @Override
    protected String getTagName() {
        return ItemPneumaticArmor.NBT_SPEED_BOOST;
    }

    @Override
    protected String getPrefix() {
        return "Boost: ";
    }

    @Override
    protected String getSuffix() {
        return "%";
    }
}
