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

package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
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
     * {@link me.desht.pneumaticcraft.api.item.IItemRegistry#makeItemAirHandlerProvider(ItemStack)}
     * or create your own implementation; either way, it can be returned from
     * {@link net.minecraft.world.item.Item#initCapabilities(ItemStack, CompoundTag)}.
     */
    abstract class Provider implements IAirHandlerItem, ICapabilityProvider {
    }
}
