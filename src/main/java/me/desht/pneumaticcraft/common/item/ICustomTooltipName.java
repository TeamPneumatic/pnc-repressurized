package me.desht.pneumaticcraft.common.item;

import net.minecraft.item.ItemStack;

/**
 * Allows a group of items/blocks to share a translation key, e.g. see plastic construction bricks
 */
public interface ICustomTooltipName {
    String getCustomTooltipTranslationKey();

    static String getTranslationKey(ItemStack stack) {
        return "gui.tooltip." + (stack.getItem() instanceof ICustomTooltipName ?
                ((ICustomTooltipName) stack.getItem()).getCustomTooltipTranslationKey() :
                stack.getTranslationKey());
    }
}
