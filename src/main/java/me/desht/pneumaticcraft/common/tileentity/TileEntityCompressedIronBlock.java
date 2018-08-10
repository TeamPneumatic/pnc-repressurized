package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.network.DescSynced;
import net.minecraft.util.EnumFacing;

public class TileEntityCompressedIronBlock extends TileEntityTickableBase implements IHeatExchanger, IComparatorSupport {

    protected final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    @DescSynced
    private int heatLevel = 10;
    private int oldComparatorOutput = 0;

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
            heatLevel = HeatUtil.getHeatLevelForTemperature(heatExchanger.getTemperature());

            int comparatorOutput = HeatUtil.getComparatorOutput((int) heatExchanger.getTemperature());
            if (oldComparatorOutput != comparatorOutput) {
                oldComparatorOutput = comparatorOutput;
                updateNeighbours();
            }
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public int getComparatorValue() {
        return HeatUtil.getComparatorOutput((int) heatExchanger.getTemperature());
    }

}
