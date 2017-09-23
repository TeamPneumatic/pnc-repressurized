package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

public class TileEntityAdvancedAirCompressor extends TileEntityAirCompressor implements IHeatExchanger {
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();

    public TileEntityAdvancedAirCompressor() {
        super(20, 25, 10000);
        heatExchanger.setThermalCapacity(100);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return heatExchanger;
    }

    @Override
    protected void onFuelBurn(int burnedFuel) {
        heatExchanger.addHeat(burnedFuel / 2D);
    }

    @Override
    public int getBaseProduction() {
        return PneumaticValues.PRODUCTION_ADVANCED_COMPRESSOR;
    }

    @Override
    public int getEfficiency() {
        return getEfficiency(heatExchanger.getTemperature());
    }

    public static int getEfficiency(double temperature) {
        return MathHelper.clamp((int) ((625 - temperature) / 3), 0, 100);//0% efficiency at > 350 degree C, 100% at < 50 degree C.
    }

//    @Override
//    protected float getSpeedUsageMultiplierFromUpgrades(int[] upgradeSlots) {
//        return getSpeedMultiplierFromUpgrades(upgradeSlots);//return the same as the speed multiplier, so adding speed upgrades doesn't affect the efficiency.
//    }

    @Override
    public String getName() {
        return Blockss.ADVANCED_AIR_COMPRESSOR.getUnlocalizedName();
    }
}
