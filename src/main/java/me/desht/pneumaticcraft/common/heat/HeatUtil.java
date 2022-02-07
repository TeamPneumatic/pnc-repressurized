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

import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collection;
import java.util.Locale;

public class HeatUtil {
    private static final int COMPARATOR_MIN = -200 + 273;
    private static final int COMPARATOR_MAX = 200 + 273;

    /**
     * Get a colour in (A)RGB format for the given temperature.
     * @param temperature the temperature
     * @return the colour
     */
    public static TintColor getColourForTemperature(int temperature) {
        int h;
        float s;

        if (temperature < 273) {
            h = 180 + (300 - temperature) / 5;  // 180 -> 240: cyan -> blue
            s = (273 - temperature) / 273f;
        } else if (temperature < 323) {
            h = 360;
            s = 0f;
        } else if (temperature < 873) {
            h = (int) ((temperature - 323) / 550f * 30f);  // red -> orange
            s = (temperature - 323) / 550f;
        } else {
            h = 30 + (int) ((temperature - 873) / 1400f * 15f);  // orange -> yellow (part way)
            s = 1f;
        }

        return TintColor.getHSBColor(h / 360f, s, 1f);
    }

    public static int getComparatorOutput(int temperature) {
        temperature = temperature - 200;
        if (temperature < COMPARATOR_MIN) {
            return 0;
        } else if (temperature > COMPARATOR_MAX) {
            return 15;
        } else {
            return (temperature - COMPARATOR_MIN) * 16 / (COMPARATOR_MAX - COMPARATOR_MIN);
        }
    }

    /**
     * Get the efficiency of a heat-using machine based on its temperature.
     * @param temperature the temperature, in Kelvin
     * @return efficiency percentage
     */
    public static int getEfficiency(int temperature) {
        // 0% efficiency at > 350 degree C, 100% at < 50 degree C.
        return Mth.clamp(((625 - temperature) / 3), 0, 100);
    }

    public static Component formatHeatString(int tempK) {
        return formatHeatString((tempK - 273) + "°C");
    }

    public static Component formatHeatString(Direction face, int tempK) {
        return formatHeatString(face, (tempK - 273) + "°C");
    }

    public static Component formatHeatString(String temp) {
        return PneumaticCraftUtils.xlate("pneumaticcraft.waila.temperature")
                .append(ChatFormatting.WHITE + temp)
                .withStyle(ChatFormatting.GRAY);
    }

    public static Component formatHeatString(Direction face, String temp) {
        return PneumaticCraftUtils.xlate("pneumaticcraft.waila.temperature." + face.toString().toLowerCase(Locale.ROOT))
                .append(ChatFormatting.WHITE + temp)
                .withStyle(ChatFormatting.GRAY);
    }

    public static int countExposedFaces(Collection<? extends BlockEntity> teList) {
        int exposed = 0;

        for (BlockEntity te : teList) {
            for (Direction face : Direction.values()) {
                if (te.getLevel().isEmptyBlock(te.getBlockPos().relative(face))) {
                    exposed++;
                }
            }
        }
        return exposed;
    }
}
