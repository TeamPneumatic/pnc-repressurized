package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * An air handler for items.
 */
public interface IAirHandlerItem extends IAirHandler {
    /**
     * Get the itemstack container currently acted on by this air handler.
     */
    @Nonnull
    ItemStack getContainer();
}
