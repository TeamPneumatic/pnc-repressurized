package me.desht.pneumaticcraft.api.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * To be implemented on equippable items.  When equipped, the item can modify the player's
 * field of view.
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
    float getFOVModifier(ItemStack stack, EntityPlayer player, EntityEquipmentSlot slot);
}
