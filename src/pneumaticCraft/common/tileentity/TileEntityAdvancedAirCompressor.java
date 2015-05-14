package pneumaticCraft.common.tileentity;

import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.lib.PneumaticValues;

public class TileEntityAdvancedAirCompressor extends TileEntityAirCompressor implements IHeatExchanger{
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatExchangerLogic();

    public TileEntityAdvancedAirCompressor(){
        super(20, 25, 10000);
        heatExchanger.setThermalCapacity(100);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(ForgeDirection side){
        return heatExchanger;
    }

    @Override
    protected void onFuelBurn(int burnedFuel){
        heatExchanger.addHeat(burnedFuel / 2D);
    }

    @Override
    public int getBaseProduction(){
        return PneumaticValues.PRODUCTION_ADVANCED_COMPRESSOR;
    }

    @Override
    public int getEfficiency(){
        return getEfficiency(heatExchanger.getTemperature());
    }

    public static int getEfficiency(double temperature){
        return MathHelper.clamp_int((int)((625 - temperature) / 3), 0, 100);//0% efficiency at > 350 degree C, 100% at < 50 degree C.
    }

    @Override
    protected float getSpeedUsageMultiplierFromUpgrades(int[] upgradeSlots){
        return getSpeedMultiplierFromUpgrades(upgradeSlots);//return the same as the speed multiplier, so adding speed upgrades doesn't affect the efficiency.
    }

    @Override
    public String getInventoryName(){

        return Blockss.advancedAirCompressor.getUnlocalizedName();
    }
}
