package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HeatExchangerLogicAmbient extends HeatExchangerLogicConstant {
    static final double BASE_AMBIENT_TEMP = 300; // temperature of water in the registry

    private static final HeatExchangerLogicAmbient DEFAULT_AIR_EXCHANGER = new HeatExchangerLogicAmbient(BASE_AMBIENT_TEMP);

    public static HeatExchangerLogicAmbient atPosition(World world, BlockPos pos) {
        if (PNCConfig.Common.Heat.ambientTempBiomeModifier == 0
                && PNCConfig.Common.Heat.ambientTempHeightModifier == 0) {
            return DEFAULT_AIR_EXCHANGER;
        }

        // biome temp of 0.8 is plains: let's call that the baseline
        // max (vanilla) is 2.0 for desert / nether, min is -0.5 for snowy taiga mountains
        float t = world.getBiome(pos).getDefaultTemperature() - 0.8f;

        int h = 0;
        if (pos.getY() > 80) {
            h = 80 - pos.getY();
        } else if (pos.getY() < 40) {
            h = 40 - pos.getY();
        }

        double temp = BASE_AMBIENT_TEMP
                + PNCConfig.Common.Heat.ambientTempBiomeModifier * t
                + PNCConfig.Common.Heat.ambientTempHeightModifier * h;

        return new HeatExchangerLogicAmbient(temp);
    }

    private HeatExchangerLogicAmbient(double temperature) {
        super(temperature, PNCConfig.Common.Heat.airThermalResistance);
    }
}
