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

package me.desht.pneumaticcraft.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

/**
 * MineChess
 *
 * @author MineMaarten
 *         www.minemaarten.com
 */

public abstract class LocationDoublePacket {
    protected double x, y, z;

    LocationDoublePacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    LocationDoublePacket(Vector3d v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    LocationDoublePacket(PacketBuffer buffer) {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }
}
