package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Collection;

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
            s = 0.25f + (273 - temperature) / 273f * 0.75f;
        } else if (temperature < 323) {
            h = 360;
            s = 0f;
        } else if (temperature < 873) {
            h = (int) (temperature / 873f * 30f);  // red -> orange
            s = temperature / 873f;
        } else {
            h = 30 + (int) (temperature / 2273f * 10f);  // orange -> yellow (part way)
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
        return MathHelper.clamp(((625 - temperature) / 3), 0, 100);
    }

    public static ITextComponent formatHeatString(int tempK) {
        return formatHeatString((tempK - 273) + "°C");
    }

    public static ITextComponent formatHeatString(Direction face, int tempK) {
        return formatHeatString(face, (tempK - 273) + "°C");
    }

    public static ITextComponent formatHeatString(String temp) {
        return PneumaticCraftUtils.xlate("pneumaticcraft.waila.temperature")
                .appendText(TextFormatting.WHITE.toString() + temp)
                .applyTextStyles(TextFormatting.GRAY);
    }

    public static ITextComponent formatHeatString(Direction face, String temp) {
        return PneumaticCraftUtils.xlate("pneumaticcraft.waila.temperature." + face.toString().toLowerCase())
                .appendText(TextFormatting.WHITE.toString() + temp)
                .applyTextStyles(TextFormatting.GRAY);
    }

    public static int countExposedFaces(Collection<? extends TileEntity> teList) {
        int exposed = 0;

        for (TileEntity te : teList) {
            for (Direction face : Direction.values()) {
                if (te.getWorld().isAirBlock(te.getPos().offset(face))) {
                    exposed++;
                }
            }
        }
        return exposed;
    }
}
