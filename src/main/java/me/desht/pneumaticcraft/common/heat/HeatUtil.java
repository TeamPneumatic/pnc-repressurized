package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Collection;

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

    /**
     * Get the efficiency of an advanced compressor based on its temperature.
     * @param temperature the temperature
     * @return efficiency percentage
     */
    public static int getEfficiency(int temperature) {
        // 0% efficiency at > 350 degree C, 100% at < 50 degree C.
        return MathHelper.clamp(((625 - temperature) / 3), 0, 100);
    }

    public static ITextComponent formatHeatString(int tempK) {
        return PneumaticCraftUtils.xlate("waila.temperature")
                .appendText(TextFormatting.WHITE.toString() + (tempK - 273) + "°C")
                .applyTextStyles(TextFormatting.GRAY);
    }

    public static ITextComponent formatHeatString(Direction face, int tempK) {
        return PneumaticCraftUtils.xlate("waila.temperature." + face.toString().toLowerCase())
                .appendText(TextFormatting.WHITE.toString() + (tempK - 273) + "°C")
                .applyTextStyle(TextFormatting.GRAY);
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
