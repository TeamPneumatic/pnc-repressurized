package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IAirHandlerItem extends IAirHandler {
    /**
     * Get the container currently acted on by this air handler.
     */
    @Nonnull
    ItemStack getContainer();
}
