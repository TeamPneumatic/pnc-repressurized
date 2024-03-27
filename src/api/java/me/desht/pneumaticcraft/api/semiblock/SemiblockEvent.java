/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.semiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public class SemiblockEvent extends Event {
    private final Level world;
    private final BlockPos pos;
    private final ISemiBlock semiblock;

    private SemiblockEvent(Level world, BlockPos pos, ISemiBlock semiblock) {
        this.world = world;
        this.pos = pos;
        this.semiblock = semiblock;
    }

    public Level getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public ISemiBlock getSemiblock() {
        return semiblock;
    }

    public Direction getSide() {
        return semiblock instanceof IDirectionalSemiblock ? ((IDirectionalSemiblock) semiblock).getSide() : null;
    }

    /**
     * Fired when a semiblock has been added to the world. This event is not cancelable.
     */
    public static class PlaceEvent extends SemiblockEvent {
        public PlaceEvent(Level world, BlockPos pos, ISemiBlock semiBlock) {
            super(world, pos, semiBlock);
        }
    }

    /**
     * Fired when a semiblock has been removed from the world. This event is not cancelable.
     */
    public static class BreakEvent extends SemiblockEvent {
        public BreakEvent(Level world, BlockPos pos, ISemiBlock semiBlock) {
            super(world, pos, semiBlock);
        }
    }
}
