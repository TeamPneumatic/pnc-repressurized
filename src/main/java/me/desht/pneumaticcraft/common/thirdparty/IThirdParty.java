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

package me.desht.pneumaticcraft.common.thirdparty;

public interface IThirdParty {
    /**
     * Called on both client and server in the mod creation thread from the mod constructor. Shouldn't do anything other
     * than add event handlers here, or set simple availability flags. No guarantee that the third party mod's
     * constructor has run yet.
     */
    default void preInit() {}

    /**
     * Called on both client and server after any registry objects are created, in the mod creation thread.
     */
    default void init() {}

    /**
     * Called on both client and server after any registry objects are created, on a scheduled tick (so in the main
     * execution thread).
     */
    default void postInit() {}

    /**
     * Called client-side after registry objects are created, in the mod creation thread.
     */
    default void clientInit() {}

    default ThirdPartyManager.ModType modType() {
        return null;
    }
}
