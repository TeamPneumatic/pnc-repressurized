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
 * Implement this for items that can get programmed in a Programmer.
 * <p>
 * For now the only thing you can do with this is make program storages; in future, there may be more applications.
 * Puzzle pieces will be written onto the implementer's itemstack NBT, under the NBT tag "pneumaticcraft:progWidgets".
 */
public interface IProgrammable {
    String NBT_WIDGETS = "pneumaticcraft:progWidgets";

    /**
     * If this method returns true, this stack may be programmed.  Can be used to limit programmability by data components,
     * for example.
     *
     * @param stack the item stack to check
     * @return true if the item can be programmed, false otherwise
     */
    boolean canProgram(ItemStack stack);

    /**
     * Check if Programming Puzzles are needed to program this item. When returned false, it's free to program.
     * Drones and Network API's return true in PneumaticCraft, Network Storages return false.
     *
     * @param stack the item stack to check
     * @return true if puzzle pieces are required by the Programmer to program the item
     */
    boolean usesPieces(ItemStack stack);

    /**
     * Check if the item should have a tooltip added detailing the puzzle piece used to program it.
     *
     * @return true if the item should have tooltip information added
     */
    boolean showProgramTooltip();

    /**
     * Convenience method to check if an arbitrary itemstack is programmable.
     * @param stack the stack to check
     * @return true if programmable, false otherwise
     */
    static boolean isProgrammable(ItemStack stack) {
        return stack.getItem() instanceof IProgrammable p && p.canProgram(stack);
    }
}
