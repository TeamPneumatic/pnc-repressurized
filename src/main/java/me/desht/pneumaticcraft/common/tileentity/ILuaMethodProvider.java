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

import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;

/**
 * A Tile Entity which makes some of its functionality available via Lua methods for calling from other mods
 * (e.g. ComputerCraft)
 */
public interface ILuaMethodProvider {
    /**
     * Get this TE's method registry object.  This should be created in the TE constructor, but not populated
     * with methods yet.
     * @return the method registry
     */
    LuaMethodRegistry getLuaMethodRegistry();

    /**
     * Get a unique identifier for this type of method provider.  The TE's type (a registry ID) is a good choice.
     * @return a unique string identifier
     */
    String getPeripheralType();

    /**
     * Called lazily to populate the method registry with the methods.
     * @param registry the registry to populate
     */
    void addLuaMethods(LuaMethodRegistry registry);
}
