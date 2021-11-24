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

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.energy.EnergyStorage;

public class PneumaticEnergyStorage extends EnergyStorage {
    public PneumaticEnergyStorage(int capacity) {
        super(capacity);
    }

    public void writeToNBT(CompoundNBT tag) {
        tag.putInt("Energy", energy);
    }

    public void readFromNBT(CompoundNBT tag) {
        energy = tag.getInt("Energy");
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
