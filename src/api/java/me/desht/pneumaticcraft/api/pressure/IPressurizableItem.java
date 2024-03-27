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

package me.desht.pneumaticcraft.api.pressure;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.world.item.ItemStack;

/**
 * Convenience interface; if your item implements this, you can take advantage of PneumaticCraft's built-in item
 * air handling functionality, while providing the
 * {@link me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem} capability to access the item's air information.
 * <p>
 * You don't <em>have to</em> implement this interface in your air-handling items, but it will make things easier;
 * the alternative is to provide a custom implementation of {@link me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem}
 * for your capability, which is more work.
 * <p>
 * @see me.desht.pneumaticcraft.api.item.IItemRegistry#makeItemAirHandler(ItemStack)
 */
public interface IPressurizableItem {
    /**
     * Get the base item volume before any volume upgrades are added.
     *
     * @return the base volume
     */
    int getBaseVolume();

    /**
     * Get the number of volume upgrades currently in this ItemStack.
     *
     * @param stack the ItemStack to check
     * @return the number of installed volume upgrades
     */
    int getVolumeUpgrades(ItemStack stack);

    /**
     * Get the amount of air currently held in this ItemStack.
     *
     * @param stack the ItemStack to check
     * @return the amount of air, in mL
     */
    int getAir(ItemStack stack);

    /**
     * Get the maximum pressure to which this item can be charged. Note that items (unlike machines) don't tend to
     * explode when they reach their pressure limit; they just stop charging.
     *
     * @return the maximum pressure for this item
     */
    float getMaxPressure();

    /**
     * The effective volume is the item's base volume, modified by both the volume upgrades installed in the item,
     * and any extra registered modifiers (e.g. CoFH Holding enchantment).
     *
     * @param stack the ItemStack to check
     * @return the effective, final, volume to use
     */
    default int getEffectiveVolume(ItemStack stack) {
        int upgradedVolume = PressureHelper.getUpgradedVolume(getBaseVolume(), getVolumeUpgrades(stack));
        return PneumaticRegistry.getInstance().getItemRegistry().getModifiedVolume(stack, upgradedVolume);
    }

    /**
     * Get the pressure for this item. This is simply the air in the item, divided by the effective volume of the item.
     *
     * @param stack the ItemStack to check
     * @return the item's current pressure, in bar
     */
    default float getPressure(ItemStack stack) {
        return (float) getAir(stack) / getEffectiveVolume(stack);
    }
}
