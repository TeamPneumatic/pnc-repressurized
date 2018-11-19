package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

public class HeatUtil {
    private static final int MIN_HEAT_LEVEL_TEMPERATURE = -200 + 273;
    private static final int MAX_HEAT_LEVEL_TEMPERATURE = 200 + 273;

    private static final float[][] HEAT_TINT_MAP = new float[20][];

    static {
        for (int i = 0; i < 20; i++) {
            if (i > 11) {
                float greenAndBlue = 1 - (i - 11) / 10F;
                HEAT_TINT_MAP[i] = new float[]{1F, greenAndBlue, greenAndBlue * 0.9F};
            } else if (i < 11) {
                float redAndGreen = i / 10F;
                HEAT_TINT_MAP[i] = new float[]{redAndGreen * 0.9F, redAndGreen, 1F};
            } else {
                HEAT_TINT_MAP[i] = new float[] { 1F, 1F, 1F };
            }
        }
    }

    public static int getHeatLevelForTemperature(double temperature) {
        if (temperature < MIN_HEAT_LEVEL_TEMPERATURE) {
            return 0;
        } else if (temperature > MAX_HEAT_LEVEL_TEMPERATURE) {
            return 19;
        } else {
            return (int) ((temperature - MIN_HEAT_LEVEL_TEMPERATURE) * 20 / (MAX_HEAT_LEVEL_TEMPERATURE - MIN_HEAT_LEVEL_TEMPERATURE));
        }
    }

    public static float[] getColorForHeatLevel(int heatLevel) {
        return HEAT_TINT_MAP[MathHelper.clamp(heatLevel, 0, 19)];
    }

    public static int getComparatorOutput(int temperature) {
        temperature = temperature - 200;
        if (temperature < MIN_HEAT_LEVEL_TEMPERATURE) {
            return 0;
        } else if (temperature > MAX_HEAT_LEVEL_TEMPERATURE) {
            return 15;
        } else {
            return (temperature - MIN_HEAT_LEVEL_TEMPERATURE) * 16 / (MAX_HEAT_LEVEL_TEMPERATURE - MIN_HEAT_LEVEL_TEMPERATURE);
        }
    }

    public static String formatHeatString(int tempK) {
        return PneumaticCraftUtils.xlate("waila.temperature") + " " + TextFormatting.WHITE + (tempK - 273) + "°C";
    }

    public static String formatHeatString(EnumFacing face, int tempK) {
        return PneumaticCraftUtils.xlate("waila.temperature." + face.toString().toLowerCase()) + " " + TextFormatting.WHITE + (tempK - 273) + "°C";
    }
}
