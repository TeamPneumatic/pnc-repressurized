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

import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.lang.ref.WeakReference;
import java.util.BitSet;
import java.util.EnumMap;

class CachedTileNeighbours {
    private final BitSet known = new BitSet(6);
    private final EnumMap<Direction,WeakReference<TileEntity>> neighbours = new EnumMap<>(Direction.class);
    private final TileEntity owner;

    public CachedTileNeighbours(TileEntity owner) {
        this.owner = owner;
        for (Direction d : DirectionUtil.VALUES) {
            neighbours.put(d, new WeakReference<>(null));
        }
    }

    public TileEntity getCachedNeighbour(Direction dir) {
        if (owner.getLevel() == null) return null;
        TileEntity res = known.get(dir.get3DDataValue()) ? neighbours.get(dir).get() : findNeighbour(dir);
        if (res != null && res.isRemoved()) {
            // shouldn't happen, but let's be defensive
            res = findNeighbour(dir);
        }
        return res;
    }

    private TileEntity findNeighbour(Direction dir) {
        BlockPos pos2 = owner.getBlockPos().relative(dir);
        TileEntity te = owner.getLevel().isAreaLoaded(pos2, 0) ? owner.getLevel().getBlockEntity(pos2) : null;
        neighbours.put(dir, new WeakReference<>(te));
        known.set(dir.get3DDataValue());
        return te;
    }

    public void purge() {
        known.clear();
    }
}
