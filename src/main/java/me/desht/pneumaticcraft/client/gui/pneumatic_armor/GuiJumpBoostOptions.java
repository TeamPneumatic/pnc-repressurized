package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.JumpBoostUpgradeHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;

public class GuiJumpBoostOptions extends GuiSliderOptions<JumpBoostUpgradeHandler> {
    public GuiJumpBoostOptions(IGuiScreen screen, JumpBoostUpgradeHandler handler) {
        super(screen, handler);
    }

    @Override
    protected String getTagName() {
        return ItemPneumaticArmor.NBT_JUMP_BOOST;
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
