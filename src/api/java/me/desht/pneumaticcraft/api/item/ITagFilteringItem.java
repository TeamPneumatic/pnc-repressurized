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

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Implement this on items which can be used to do a match by item tags.  E.g. the Tag Filter item.  Such items can
 * be passed as the first parameter to {@link IItemRegistry#doesItemMatchFilter(ItemStack, ItemStack, boolean, boolean, boolean)}
 */
public interface ITagFilteringItem {
    /**
     * Does the given item's tags have a non-empty intersection with the given filter stack's configured tag list?
     * @param filterStack the stack to filter against; should be of an item implementing this interface
     * @param item the item to check
     * @return true if there's an intersection, false otherwise
     */
    boolean matchTags(ItemStack filterStack, Item item);
}
