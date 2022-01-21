/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

/**
 * Allows a group of items/blocks to share a translation key, e.g. see plastic construction bricks
 * Only to be used client-side!
 */
public interface ICustomTooltipName {
    String getCustomTooltipTranslationKey();

    static String getTranslationKey(ItemStack stack, boolean brief) {
        String key = "gui.tooltip." + (stack.getItem() instanceof ICustomTooltipName ?
                ((ICustomTooltipName) stack.getItem()).getCustomTooltipTranslationKey() :
                stack.getDescriptionId());

        // brief version (if exists) is used for item/block tooltips
        // long/default version is used in JEI info tab and GUI side tab
        if (brief && I18n.exists(key + ".brief")) {
            return key + ".brief";
        } else {
            return key;
        }
    }
}
