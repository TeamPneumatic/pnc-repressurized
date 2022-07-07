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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum TemperatureCategory implements StringRepresentable {
    SUPER_COLD("super_cold"),
    VERY_COLD("very_cold"),
    COLD("cold"),
    COOL("cool"),
    NORMAL("normal"),
    WARM("warm"),
    HOT("hot"),
    VERY_HOT("very_hot"),
    SUPER_HOT("super_hot");

    private final String name;
    private final ResourceLocation textureLoc;

    TemperatureCategory(String name) {
        this.name = name;
        this.textureLoc = Textures.modelTexture("heat_frame_" + name + ".png");
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

    public ResourceLocation getTextureLocation() {
        return textureLoc;
    }
}
