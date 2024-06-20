/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public class AirHandlerItemStack implements IAirHandlerItem {
    private final ItemStack container;
    private final IPressurizableItem pressurizable;
    private int baseVolume;
    private final float maxPressure;

    public AirHandlerItemStack(ItemStack container) {
        Validate.isTrue(container.getItem() instanceof IPressurizableItem, "itemstack " + container + " must be an IPressurizableItem!");
        this.container = container;
        this.pressurizable = (IPressurizableItem) container.getItem();
        this.baseVolume = pressurizable.getBaseVolume();
        this.maxPressure = pressurizable.getMaxPressure();
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public float getPressure() {
        float pressure = pressurizable.getPressure(container);
        if (pressure > maxPressure) {
            // this isn't impossible, e.g. enchant an item with CoFH Holding, pressurize, then disenchant...
            // best option in this case is just to reduce air to the max amount it can actually hold
            container.set(ModDataComponents.AIR, (int) (maxPressure * getVolume()));
            return maxPressure;
        }
        return pressure;
    }

    @Override
    public int getAir() {
        return pressurizable.getAir(container);
    }

    @Override
    public void addAir(int amount) {
        if (container.getCount() != 1) return;

        int currentAir = getAir();
        int newAir = currentAir + amount;
        if (newAir != 0) {
            container.set(ModDataComponents.AIR, currentAir + amount);
        } else {
            container.remove(ModDataComponents.AIR);
        }
    }

    @Override
    public int getBaseVolume() {
        return baseVolume;
    }

    @Override
    public void setBaseVolume(int newBaseVolume) {
        this.baseVolume = newBaseVolume;
    }

    @Override
    public int getVolume() {
        return pressurizable.getEffectiveVolume(container);
    }

    @Override
    public float maxPressure() {
        return maxPressure;
    }
}
