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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public interface IShiftScrollable {
    /**
     * Called both client- and server-side when a player shift-scrolls the mouse wheel, while holding an item
     * (in main hand) which implements this interface.
     *  @param player player doing the shift-scrolling
     * @param forward true if the mouse wheel was rotated up, false if rotated down
     * @param hand
     */
    void onShiftScrolled(PlayerEntity player, boolean forward, Hand hand);
}
