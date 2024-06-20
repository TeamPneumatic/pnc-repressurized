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

import me.desht.pneumaticcraft.api.item.ItemVolumeModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Base functionality for all air handlers.  This is also used by entities which can be pressurized, e.g. drones.
 */
public interface IAirHandler {
    /**
     * Get the current pressure for this handler.
     *
     * @return the current pressure
     */
    float getPressure();

    /**
     * Returns the amount of air in this handler.  Note: amount of air = pressure * volume.
     *
     * @return the air in this air handler
     */
    int getAir();

    /**
     * Adds air to this handler.
     *
     * @param amount amount of air to add in mL, may be negative.
     */
    void addAir(int amount);

    /**
     * Gets the base volume of this handler, before any Volume Upgrades are taken into account. When the volume
     * decreases, the pressure will remain the same, meaning air will be lost. When the volume increases, the air
     * remains the same, meaning the pressure will drop.
     *
     * @return the base volume
     */
    int getBaseVolume();

    /**
     * Set the base volume of this air handler. May be useful if the base volume depends on factors other than the
     * number of volume upgrades; for example, the Pressure Chamber multiblock uses this to increase the volume based
     * on the multiblock size.
     *
     * @param newBaseVolume the new base volume
     */
    void setBaseVolume(int newBaseVolume);

    /**
     * Get the effective volume of this air handler. This may have been increased by Volume Upgrades, and also other
     * external modifiers (see {@link me.desht.pneumaticcraft.api.item.IItemRegistry#registerPneumaticVolumeModifier(ItemVolumeModifier)}).
     *
     * @return the effective volume, in mL
     */
    int getVolume();

    /**
     * Get the maximum pressure this handler can take.  Behaviour when more air is added is implementation-dependent
     * (e.g. items tend to stop accepting air, while blocks/machines tend to explode)
     *
     * @return the maximum pressure for this handler
     */
    float maxPressure();

}
