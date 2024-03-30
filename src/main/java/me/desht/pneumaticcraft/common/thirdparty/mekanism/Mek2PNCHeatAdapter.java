package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerAdapter;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import mekanism.api.heat.ISidedHeatHandler;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Mek2PNCHeatAdapter extends IHeatExchangerAdapter.Simple<ISidedHeatHandler> {
    private final double thermalResistanceMult;

    public Mek2PNCHeatAdapter(Direction side, ISidedHeatHandler foreignHeatCap, double ambientTemperature, double thermalResistanceMult) {
        super(side, foreignHeatCap, ambientTemperature);

        this.thermalResistanceMult = thermalResistanceMult;
    }

    public static IHeatExchangerLogic maybe(BlockEntity blockEntity, Direction direction) {
        if (blockEntity instanceof ISidedHeatHandler handler) {
            double ambient = HeatExchangerLogicAmbient.getAmbientTemperature(blockEntity.getLevel(), blockEntity.getBlockPos());
            return new Mek2PNCHeatAdapter(direction, handler, ambient, getResistanceMultiplier(blockEntity));
        } else {
            return null;
        }
    }

    // Mekanism transmitters (i.e. Thermodynamic Conductors or TC's) get special treatment, due to the
    // way Mek handles heat; Mek heater BE's will continue to push heat out to reduce their own
    // temperature back to 300K (Mek ambient), regardless of how hot the sink is.
    // TC's, with a heat capacity of only 1.0, get very hot, very fast.
    // This poses a problem for PNC:R, since it handles heat by temperature delta between the two blocks, and
    // TC's will overheat PNC machines really, really quickly.  As a kludge, we give TC's a very high thermal
    // resistance when connected to PNC:R blocks, limiting the rate with which a PNC:R heat exchanger will
    // equalise heat directly. This doesn't stop the TC from *pushing* heat, though.
    private static double getResistanceMultiplier(BlockEntity be) {
        return PneumaticCraftUtils.getRegistryName(BuiltInRegistries.BLOCK_ENTITY_TYPE, be.getType()).map(name -> {
            if (name.getNamespace().equals(ModIds.MEKANISM)
                    && (name.getPath().equals("quantum_entangloporter") || name.getPath().endsWith("thermodynamic_conductor"))) {
                return 10_000_000;
            } else {
                return 1;
            }
        }).orElse(1);
    }

    @Override
    public double getTemperature() {
        return foreignHeatCap.getTemperature(0);
    }

    @Override
    public double getThermalResistance() {
        return foreignHeatCap.getInverseConduction(0) * thermalResistanceMult;
    }

    @Override
    public double getThermalCapacity() {
        return foreignHeatCap.getHeatCapacity(0);
    }

    @Override
    public void addHeat(double amount) {
        if (amount > 0) {
            // thermal efficiency factor can't be 0 at this point, or adapter caps would not have been added
            foreignHeatCap.handleHeat(amount / ConfigHelper.common().integration.mekThermalEfficiencyFactor.get());
        }
    }
}
