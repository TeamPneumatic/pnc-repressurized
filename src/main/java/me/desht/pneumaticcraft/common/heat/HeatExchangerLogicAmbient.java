package me.desht.pneumaticcraft.common.heat;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.desht.pneumaticcraft.common.config.PNCConfig.Common.Heat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class HeatExchangerLogicAmbient extends HeatExchangerLogicConstant {
    static final double BASE_AMBIENT_TEMP = 300; // temperature of water in the registry

    private static final HeatExchangerLogicAmbient DEFAULT_AIR_EXCHANGER = new HeatExchangerLogicAmbient(BASE_AMBIENT_TEMP);
    private static final Int2ObjectOpenHashMap<HeatExchangerLogicAmbient> exchangers = new Int2ObjectOpenHashMap<>();

    public static HeatExchangerLogicAmbient atPosition(IWorld world, BlockPos pos) {
        if (Heat.ambientTempBiomeModifier == 0 && Heat.ambientTempHeightModifier == 0) {
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

        int temp = (int) (BASE_AMBIENT_TEMP + Heat.ambientTempBiomeModifier * t + Heat.ambientTempHeightModifier * h);
        return exchangers.computeIfAbsent(temp, HeatExchangerLogicAmbient::new);
    }

    private HeatExchangerLogicAmbient(double temperature) {
        super(temperature, Heat.airThermalResistance);
    }
}
