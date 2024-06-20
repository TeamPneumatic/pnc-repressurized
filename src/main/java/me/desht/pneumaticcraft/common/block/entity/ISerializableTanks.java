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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents a block entity which stores tank data that should be serialized onto the dropped item stack when the block
 * is broken, and deserialized back to the block entity when the block is placed down again.
 */
@FunctionalInterface
public interface ISerializableTanks {
    /**
     * Get a mapping of all tanks; this maps a data component (which must be for a {@link SimpleFluidContent})
     * to a PNC fluid tank object.
     *
     * @return a map of tag names to tanks
     */
    @Nonnull
    Map<DataComponentType<SimpleFluidContent>, PNCFluidTank> getSerializableTanks();

    default void deserializeTanks(HolderLookup.Provider provider, CompoundTag tag) {
        getSerializableTanks().forEach((comp, tank) -> tank.readFromNBT(provider, tag.getCompound(comp.toString())));
    }

    default CompoundTag serializeTanks(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        getSerializableTanks().forEach((comp, tank) -> {
            if (!tank.getFluid().isEmpty()) {
                tag.put(comp.toString(), tank.writeToNBT(provider, new CompoundTag()));
            }
        });
        return tag;
    }
}
