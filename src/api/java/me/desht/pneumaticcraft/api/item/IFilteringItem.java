/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.item;

import net.minecraft.world.item.ItemStack;

/**
 * This interface may be implemented on items which can be used as filters for other items, for example the
 * Tag Filter and Classify Filter items.
 * <p>
 * Such items can be passed as the first parameter to {@link IItemRegistry#doesItemMatchFilter(ItemStack, ItemStack, boolean, boolean, boolean)}
 * <p>
 * Alternatively, it may be attached as an item capability to items using the {@link me.desht.pneumaticcraft.api.PNCCapabilities#ITEM_FILTERING}
 * capability.
 */
public interface IFilteringItem {
    /**
     * Does the given item's match the given filter stack's filtering criteria?
     * @param filterStack the stack to filter against
     * @param item the item to check
     * @return true if there's a match, false otherwise
     */
    boolean matchFilter(ItemStack filterStack, ItemStack item);
}
