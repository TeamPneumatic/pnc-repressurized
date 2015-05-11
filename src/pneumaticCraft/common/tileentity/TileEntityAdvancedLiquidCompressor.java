package pneumaticCraft.common.tileentity;

import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.lib.PneumaticValues;

public class TileEntityAdvancedLiquidCompressor extends TileEntityLiquidCompressor implements IHeatExchanger{

    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatExchangerLogic();

    public TileEntityAdvancedLiquidCompressor(){
        super(20, 25, 10000);
        heatExchanger.setThermalCapacity(100);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(ForgeDirection side){
        return heatExchanger;
    }

    @Override
    protected void onFuelBurn(int burnedFuel){
        heatExchanger.addHeat(burnedFuel / 20D);
    }

    @Override
    public int getBaseProduction(){
        return PneumaticValues.PRODUCTION_ADVANCED_COMPRESSOR;
    }

    @Override
    public int getEfficiency(){
        return TileEntityAdvancedAirCompressor.getEfficiency(heatExchanger.getTemperature());
    }

    @Override
    protected float getSpeedUsageMultiplierFromUpgrades(int[] upgradeSlots){
        return getSpeedMultiplierFromUpgrades(upgradeSlots);//return the same as the speed multiplier, so adding speed upgrades doesn't affect the efficiency.
    }

    @Override
    public String getInventoryName(){

        return Blockss.advancedLiquidCompressor.getUnlocalizedName();
    }
}
