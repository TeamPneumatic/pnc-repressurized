package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.ItemStack;

/**
 * Implement this for items that can get programmed in a Programmer.
 * <p>
 * For now the only thing you can do with this is make program storages, later more interaction with programming puzzles
 * is planned.  Puzzle pieces will be written onto the implementer's itemstack NBT, using the "progWidget" tag.
 */
public interface IProgrammable {

    /**
     * When returned true, this stack is allowed to be programmed.
     * Used to allow certain damage values to be programmed while others can't.
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

}
