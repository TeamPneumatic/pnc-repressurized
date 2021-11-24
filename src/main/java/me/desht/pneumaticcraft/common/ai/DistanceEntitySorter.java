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

package me.desht.pneumaticcraft.common.ai;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Comparator;

public class DistanceEntitySorter implements Comparator<Entity> {
    private final IDroneBase drone;

    DistanceEntitySorter(IDroneBase drone) {
        this.drone = drone;
    }

    private int compare_internal(Entity entity1, Entity entity2) {
        Vector3d vec = drone.getDronePos();
        double d0 = vec.distanceToSqr(entity1.position());
        double d1 = vec.distanceToSqr(entity2.position());
        return Double.compare(d0, d1);
    }

    @Override
    public int compare(Entity p_compare_1_, Entity p_compare_2_) {
        return this.compare_internal(p_compare_1_, p_compare_2_);
    }
}
