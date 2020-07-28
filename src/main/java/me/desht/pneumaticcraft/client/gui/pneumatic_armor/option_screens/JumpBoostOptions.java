package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.JumpBoostClientHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class JumpBoostOptions extends AbstractSliderOptions<JumpBoostClientHandler> {
    public JumpBoostOptions(IGuiScreen screen, JumpBoostClientHandler handler) {
        super(screen, handler);
    }

    @Override
    protected String getTagName() {
        return ItemPneumaticArmor.NBT_JUMP_BOOST;
    }

    @Override
    protected ITextComponent getPrefix() {
        return new StringTextComponent("Boost: ");
    }

    @Override
    protected ITextComponent getSuffix() {
        return new StringTextComponent("%");
    }
}
