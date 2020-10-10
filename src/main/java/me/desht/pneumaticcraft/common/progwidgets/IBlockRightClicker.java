package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.util.ITranslatableEnum;

import java.util.Locale;

public interface IBlockRightClicker {
    enum RightClickType implements ITranslatableEnum {
        CLICK_ITEM, CLICK_BLOCK;

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.blockRightClick.clickType." + toString().toLowerCase(Locale.ROOT);
        }
    }

    boolean isSneaking();

    RightClickType getClickType();
}
