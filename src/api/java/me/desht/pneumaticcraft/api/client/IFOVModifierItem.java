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

package me.desht.pneumaticcraft.api.client;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * To be implemented on equippable items.  When equipped, the item can modify the player's field of view.
 */
public interface IFOVModifierItem {
    /**
     * Get the FOV modifer for the given item stack.  Lower values zoom in.
     *
     * @param stack the equipped item
     * @param player the player who has the item equipped
     * @param slot the equipment slot
     * @return the FOV modifier
     */
    float getFOVModifier(ItemStack stack, Player player, EquipmentSlot slot);
}
