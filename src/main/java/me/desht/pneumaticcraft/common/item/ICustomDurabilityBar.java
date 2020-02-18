package me.desht.pneumaticcraft.common.item;

import net.minecraft.item.ItemStack;

/**
 * Implement on items which show a custom durability bar *in addition to* any regular bar.
 */
public interface ICustomDurabilityBar {
    boolean shouldShowCustomDurabilityBar(ItemStack stack);

    int getCustomDurabilityColour(ItemStack stack);

    float getCustomDurability(ItemStack stack);

    /**
     * Is this item already showing a regular durability bar?  This controls if the custom bar needs a small Y
     * offset so both bars can be seen.
     * @param stack the item stack
     * @return true if the item will already be showing a normal durability bar
     */
    default boolean isShowingOtherBar(ItemStack stack) {
        return stack.getDamage() > 0;
    }
}
