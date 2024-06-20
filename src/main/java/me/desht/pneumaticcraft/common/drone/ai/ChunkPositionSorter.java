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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.IBlockOrdered.Ordering;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class ChunkPositionSorter implements Comparator<BlockPos> {
    private static final double EPSILON = 0.01;

    private final double x, y, z;
    private final Ordering order;

    ChunkPositionSorter(IDrone entity) {
        this(entity, Ordering.CLOSEST);
    }

    ChunkPositionSorter(IDrone entity, Ordering order) {
        Vec3 vec = entity.getDronePos();
        // work from middle of the block the drone is in (try to minimize inconsistency)
        x = Math.floor(vec.x) + 0.5;
        y = Math.floor(vec.y) + 0.5;
        z = Math.floor(vec.z) + 0.5;

        this.order = order;
    }

    public ChunkPositionSorter(double x, double y, double z, Ordering order) {
        this.order = order;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int compare(BlockPos c1, BlockPos c2) {
        if (order != Ordering.CLOSEST && c1.getY() != c2.getY()) {
            return order == Ordering.HIGH_TO_LOW ? c2.getY() - c1.getY() : c1.getY() - c2.getY();
        } else {
            double d = PneumaticCraftUtils.distBetweenSq(c1.getX(), c1.getY(), c1.getZ(), x, y, z)
                    - PneumaticCraftUtils.distBetweenSq(c2.getX(), c2.getY(), c2.getZ(), x, y, z);
            if (Math.abs(d) < EPSILON) {
                return c1.compareTo(c2);
            } else {
                return d < 0 ? -1 : 1;
            }
        }
    }
}
