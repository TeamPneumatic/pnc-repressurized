package me.desht.pneumaticcraft.common.heat;

import net.minecraft.util.math.MathHelper;

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
}
