package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.SpeedBoostClientHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SpeedBoostOptions extends AbstractSliderOptions<SpeedBoostClientHandler> {
    public SpeedBoostOptions(IGuiScreen screen, SpeedBoostClientHandler handler) {
        super(screen, handler);
    }

    @Override
    protected String getTagName() {
        return ItemPneumaticArmor.NBT_SPEED_BOOST;
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
