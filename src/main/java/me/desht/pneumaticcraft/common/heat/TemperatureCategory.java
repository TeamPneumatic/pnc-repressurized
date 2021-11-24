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

package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

public enum TemperatureCategory implements IStringSerializable {
    SUPER_COLD(0, "super_cold"),
    VERY_COLD(1, "very_cold"),
    COLD(2, "cold"),
    COOL(3, "cool"),
    NORMAL(4, "normal"),
    WARM(5, "warm"),
    HOT(6, "hot"),
    VERY_HOT(7, "very_hot"),
    SUPER_HOT(8, "super_hot");

    private final int index;
    private final String name;

    TemperatureCategory(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public static TemperatureCategory forTemperature(double temp) {
        if (temp < 73) {
            return SUPER_COLD;
        } else if (temp < 213) {
            return VERY_COLD;
        } else if (temp < 263) {
            return COLD;
        } else if (temp < 283) {
            return COOL;
        } else if (temp < 323) {
            return NORMAL;
        } else if (temp < 373) {
            return WARM;
        } else if (temp < 773) {
            return HOT;
        } else if (temp < 1273) {
            return VERY_HOT;
        } else {
            return SUPER_HOT;
        }
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public ResourceLocation getTextureName(String base) {
        return Textures.modelTexture(base + "_" + name + ".png");
    }
}
