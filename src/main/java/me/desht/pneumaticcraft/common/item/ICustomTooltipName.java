package me.desht.pneumaticcraft.common.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/**
 * Allows a group of items/blocks to share a translation key, e.g. see plastic construction bricks
 * Only to be used client-side!
 */
public interface ICustomTooltipName {
    String getCustomTooltipTranslationKey();

    static String getTranslationKey(ItemStack stack, boolean brief) {
        String key = "gui.tooltip." + (stack.getItem() instanceof ICustomTooltipName ?
                ((ICustomTooltipName) stack.getItem()).getCustomTooltipTranslationKey() :
                stack.getTranslationKey());

        // brief version (if exists) is used for item/block tooltips
        // long/default version is used in JEI info tab and GUI side tab
        if (brief && I18n.hasKey(key + ".brief")) {
            return key + ".brief";
        } else {
            return key;
        }
    }
}
