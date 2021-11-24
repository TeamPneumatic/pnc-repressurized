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

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Ticking tile entities should either extend this class, or implement ITickable themselves.
 * Note that the superclass, TileEntityBase, contains an implementation of tick() which
 * is used by default.
 */
public abstract class TileEntityTickableBase extends TileEntityBase implements ITickableTileEntity {
    public TileEntityTickableBase(TileEntityType type) {
        this(type, 0);
    }

    public TileEntityTickableBase(TileEntityType type, int upgradeSize) {
        super(type, upgradeSize);
    }

    @Override
    public void tick() {
        tickImpl();
    }
}
