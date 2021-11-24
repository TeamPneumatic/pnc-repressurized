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

package me.desht.pneumaticcraft.common.core;

import net.minecraft.item.Food;

public class ModFoods {
    public static final Food SOURDOUGH = new Food.Builder().nutrition(7).saturationMod(1.0f).build();
    public static final Food CHIPS = new Food.Builder().nutrition(5).saturationMod(0.6f).build();
    public static final Food COD_N_CHIPS = new Food.Builder().nutrition(12).saturationMod(1.0f).build();
    public static final Food SALMON_TEMPURA = new Food.Builder().nutrition(12).saturationMod(1.0f).build();
}
