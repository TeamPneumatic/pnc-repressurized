package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.network.DescSynced;
import net.minecraft.util.EnumFacing;

public class TileEntityCompressedIronBlock extends TileEntityBase implements IHeatExchanger, IComparatorSupport {

    protected final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    @DescSynced
    private int heatLevel = 10;
    private int oldComparatorOutput = 0;
    private static final int MIN_HEAT_LEVEL_TEMPERATURE = -200 + 273;
    private static final int MAX_HEAT_LEVEL_TEMPERATURE = 200 + 273;

    public TileEntityCompressedIronBlock() {
        heatExchanger.setThermalResistance(0.01);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return heatExchanger;
    }

    public int getHeatLevel() {
        return heatLevel;
    }

    @Override
    public void update() {
        super.update();

        if (!getWorld().isRemote) {
            heatLevel = getHeatLevelForTemperature(heatExchanger.getTemperature());

            int comparatorOutput = getComparatorOutput((int) heatExchanger.getTemperature());
            if (oldComparatorOutput != comparatorOutput) {
                oldComparatorOutput = comparatorOutput;
                updateNeighbours();
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

    public static double[] getColorForHeatLevel(int heatLevel) {
        if (heatLevel > 9) {
            double greenAndBlue = 1 - (heatLevel - 10) / 10D;
            return new double[]{1, greenAndBlue, greenAndBlue};
        } else {
            double redAndGreen = heatLevel / 10D;
            return new double[]{redAndGreen, redAndGreen, 1};
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
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

    @Override
    public int getComparatorValue() {
        return getComparatorOutput((int) heatExchanger.getTemperature());
    }

}
