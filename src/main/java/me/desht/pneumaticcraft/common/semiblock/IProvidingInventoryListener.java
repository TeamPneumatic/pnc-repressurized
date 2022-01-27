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

package me.desht.pneumaticcraft.common.semiblock;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public interface IProvidingInventoryListener {
    void notify(TileEntityAndFace teAndFace);

    record TileEntityAndFace(BlockEntity te, Direction face) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TileEntityAndFace tileEntityAndFace)) return false;
            return te.equals(tileEntityAndFace.te) &&
                    face == tileEntityAndFace.face;
        }

        @Override
        public int hashCode() {
            return Objects.hash(te, face);
        }
    }
}

