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

package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents a tile entity which stores tank data that should be serialized onto the dropped item stack when the block
 * is broken, and deserialized back to the tile entity when the block is placed down again.
 */
@FunctionalInterface
public interface ISerializableTanks {
    /**
     * Get a mapping of all tanks; this maps a tag name, which is used as the serialization key, to a fluid tank.
     *
     * @return a map of tag names to tanks
     */
    @Nonnull
    Map<String, PNCFluidTank> getSerializableTanks();

    default void deserializeTanks(CompoundNBT tag) {
        getSerializableTanks().forEach((key, tank) -> tank.readFromNBT(tag.getCompound(key)));
    }

    default CompoundNBT serializeTanks() {
        CompoundNBT tag = new CompoundNBT();
        getSerializableTanks().forEach((key, tank) -> {
            if (!tank.getFluid().isEmpty()) {
                tag.put(key, tank.writeToNBT(new CompoundNBT()));
            }
        });
        return tag;
    }
}
