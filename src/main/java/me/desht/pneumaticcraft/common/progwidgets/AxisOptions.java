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

package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;

import java.util.BitSet;

public class AxisOptions {
    public static final AxisOptions TRUE = new AxisOptions(true, true, true);

    private final BitSet options = new BitSet(3);

    public AxisOptions(boolean x, boolean y, boolean z) {
        options.set(0, x);
        options.set(1, y);
        options.set(2, z);
    }

    public boolean shouldCheck(Direction.Axis axis) {
        return options.get(axis.ordinal());
    }

    public void setCheck(Direction.Axis axis, boolean check) {
        options.set(axis.ordinal(), check);
    }

    public void writeToNBT(CompoundNBT nbt) {
        nbt.putBoolean("checkX", shouldCheck(Direction.Axis.X));
        nbt.putBoolean("checkY", shouldCheck(Direction.Axis.Y));
        nbt.putBoolean("checkZ", shouldCheck(Direction.Axis.Z));
    }

    public void readFromNBT(CompoundNBT nbt, boolean def) {
        setCheck(Direction.Axis.X, nbt.contains("checkX") ? nbt.getBoolean("checkX") : def);
        setCheck(Direction.Axis.Y, nbt.contains("checkY") ? nbt.getBoolean("checkY") : def);
        setCheck(Direction.Axis.Z, nbt.contains("checkZ") ? nbt.getBoolean("checkZ") : def);
    }

    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBoolean(shouldCheck(Direction.Axis.X));
        buffer.writeBoolean(shouldCheck(Direction.Axis.Y));
        buffer.writeBoolean(shouldCheck(Direction.Axis.Z));
    }

    public void readFromBuffer(PacketBuffer buffer) {
        setCheck(Direction.Axis.X, buffer.readBoolean());
        setCheck(Direction.Axis.Y, buffer.readBoolean());
        setCheck(Direction.Axis.Z, buffer.readBoolean());
    }
}
