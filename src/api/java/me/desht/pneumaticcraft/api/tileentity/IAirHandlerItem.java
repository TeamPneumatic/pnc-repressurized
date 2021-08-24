package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

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

    /**
     * Capability provider object for air-handling items. You can make an instance of this with
     * {@link me.desht.pneumaticcraft.api.item.IItemRegistry#makeItemAirHandlerProvider(ItemStack, float)}
     * or create your own implementation; either way, it can be returned from
     * {@link net.minecraft.item.Item#initCapabilities(ItemStack, CompoundNBT)}.
     */
    abstract class Provider implements IAirHandlerItem, ICapabilityProvider {
    }
}
