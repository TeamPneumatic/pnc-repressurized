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

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A functional interface to modify a given Pneumatic item's effective volume based on attributes of the item stack
 * (generally component data, e.g. upgrades or enchantments). Instances of this can be registered
 * with {@link IItemRegistry#registerPneumaticVolumeModifier(ItemVolumeModifier)}.
 */
@FunctionalInterface
public interface ItemVolumeModifier {
    /**
     * Given an item stack, which is a pneumatic item, and its current volume, return a new, modified volume
     * @param stack the item
     * @param oldVolume the initial volume
     * @return the modified volume
     */
    int getNewVolume(ItemStack stack, int oldVolume);

    /**
     * Add some information regarding this volume modifier for GUI display purposes; this information is
     * displayed in the Charging Station upgrade screen for the item when it's in the Charging Station.
     * @param stack the item
     * @param text information text to be appended to
     */
    default void addInfo(ItemStack stack, List<Component> text) { }
}
