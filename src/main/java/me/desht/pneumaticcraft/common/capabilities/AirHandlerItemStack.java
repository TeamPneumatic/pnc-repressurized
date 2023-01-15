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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class AirHandlerItemStack extends IAirHandlerItem.Provider {
    public static final String AIR_NBT_KEY = "pneumaticcraft:air";

    private final LazyOptional<IAirHandlerItem> holder = LazyOptional.of(() -> this);

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
            container.getOrCreateTag().putInt(AIR_NBT_KEY, (int) (maxPressure * getVolume()));
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
            container.getOrCreateTag().putInt(AIR_NBT_KEY, currentAir + amount);
        } else {
            // no air in item: clean up NBT for item stackability purposes
            if (container.hasTag()) {
                Objects.requireNonNull(container.getTag()).remove(AIR_NBT_KEY);
                if (container.getTag().isEmpty()) {
                    container.setTag(null);
                }
            }
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY.orEmpty(cap, holder);
    }
}
