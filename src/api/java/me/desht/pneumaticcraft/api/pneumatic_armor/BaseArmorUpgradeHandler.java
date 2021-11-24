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

package me.desht.pneumaticcraft.api.pneumatic_armor;

import org.apache.commons.lang3.Validate;

public abstract class BaseArmorUpgradeHandler<T extends IArmorExtensionData> implements IArmorUpgradeHandler<T> {
    int idx = -1;

    @Override
    public int getIndex() {
        return idx;
    }

    @Override
    public void setIndex(int index) {
        Validate.isTrue(index >= 0, "negative index not permitted!");
        if (idx != -1) throw new IllegalStateException("attempt to overwrite existing index");
        this.idx = index;
    }
}
