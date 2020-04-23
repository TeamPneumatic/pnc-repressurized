package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.ItemStack;

/**
 * Implement on items which can show a custom durability bar *in addition to* any regular bar.
 */
public interface ICustomDurabilityBar {
    /**
     * Check if this item should be showing its custom bar at this time.
     * @param stack the item stack
     * @return true if the custom durability bar should be displayed, false otherwise
     */
    boolean shouldShowCustomDurabilityBar(ItemStack stack);

    /**
     * Get the colour of the custom durability bar for this item, in RGB format (alpha will always be full)
     * @param stack the item stack
     * @return the colour to draw the custom durability bar
     */
    int getCustomDurabilityColour(ItemStack stack);

    /**
     * Get the actual value for the custom durability of the item, which controls the width of the drawn bar.
     * @param stack the item stack
     * @return a durability value in the range 0.0F to 1.0F
     */
    float getCustomDurability(ItemStack stack);

    /**
     * Is this item already showing a regular durability bar?  This controls if the custom bar needs a small Y
     * offset so both bars can be seen. If the regular bar is being shown, the custom bar will be directly above it;
     * if it is not being shown, the custom bar will be drawn in its place.
     * @param stack the item stack
     * @return true if the item will already be showing a normal durability bar
     */
    default boolean isShowingOtherBar(ItemStack stack) {
        return stack.getDamage() > 0;
    }
}
