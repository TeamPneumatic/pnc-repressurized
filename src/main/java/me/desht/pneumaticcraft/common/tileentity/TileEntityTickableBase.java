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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ticking tile entities should either extend this class, or implement ITickable themselves.
 * Note that the superclass, TileEntityBase, contains an implementation of tick() which
 * is used by default.
 */
public abstract class TileEntityTickableBase extends TileEntityBase {
    public TileEntityTickableBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 0);
    }

    public TileEntityTickableBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int upgradeSize) {
        super(type, pos, state, upgradeSize);
    }

    /**
     * Called on both server and client, before anything else
     */
    public void tickCommonPre() {
        getUpgradeCache().validateCache();
    }

    public void tickClient() {
    }

    public void tickServer() {
        defaultServerTick();
    }

    /**
     * Called on both server and client, after anything else
     */
    public void tickCommonPost() {
    }
}
