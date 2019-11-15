package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HeatExchangerLogicAmbient extends HeatExchangerLogicConstant {
    // todo 1.14 fluids
    public static final double BASE_AMBIENT_TEMP = 300; // FluidRegistry.WATER.getTemperature();

    private static final HeatExchangerLogicAmbient DEFAULT_AIR_EXCHANGER = new HeatExchangerLogicAmbient(BASE_AMBIENT_TEMP);

    public static HeatExchangerLogicAmbient atPosition(World world, BlockPos pos) {
        if (PNCConfig.Common.BlockHeatDefaults.ambientTempBiomeModifier == 0 && PNCConfig.Common.BlockHeatDefaults.ambientTempHeightModifier == 0) {
            return DEFAULT_AIR_EXCHANGER;
        }

        // biome temp of 0.8 is plains: let's call that the baseline
        // max (vanilla) is 2.0 for nether, min is -0.5 for cold taiga
        float t = world.getBiome(pos).getDefaultTemperature() - 0.8f;

        int h = 0;
        if (pos.getY() > 80) {
            h = 80 - pos.getY();
        } else if (pos.getY() < 40) {
            h = 40 - pos.getY();
        }

        double temp = BASE_AMBIENT_TEMP
                + PNCConfig.Common.BlockHeatDefaults.ambientTempBiomeModifier * t
                + PNCConfig.Common.BlockHeatDefaults.ambientTempHeightModifier * h;

        return new HeatExchangerLogicAmbient(temp);
    }

    private HeatExchangerLogicAmbient(double temperature) {
        super(temperature, 100);
    }
}
