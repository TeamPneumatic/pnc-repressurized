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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.List;

public interface IEntityProvider {
    List<Entity> getValidEntities(Level world);

    boolean isEntityValid(Entity entity);

    /**
     * Most, but not all, widgets have the entity filter attached as the second piece (area as the first)
     *
     * @return the 0-based position of the entity filter Text widget
     */
    default int getEntityFilterPosition() {
        return 1;
    }
}
