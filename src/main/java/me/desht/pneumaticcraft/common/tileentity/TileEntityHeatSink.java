package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityHeatSink extends TileEntityCompressedIronBlock implements IHeatExchanger {

    private final IHeatExchangerLogic airExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();

    private double ambientTemp = -1;

    public TileEntityHeatSink() {
        airExchanger.addConnectedExchanger(heatExchanger);
        airExchanger.setThermalResistance(TileEntityConstants.HEAT_SINK_THERMAL_RESISTANCE);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return side == null || side == getRotation() ? super.getHeatExchangerLogic(side) : null;
    }

    @Override
    protected EnumFacing[] getConnectedHeatExchangerSides() {
        return new EnumFacing[]{getRotation()};
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void update() {
        if (ambientTemp < 0) {
            ambientTemp = HeatExchangerLogicAmbient.atPosition(getWorld(), getPos()).getTemperature();
            airExchanger.setTemperature(ambientTemp);
        }

        super.update();

        airExchanger.update();
        airExchanger.setTemperature(ambientTemp);
    }

    public void onFannedByAirGrate() {
        heatExchanger.update();
        airExchanger.setTemperature(ambientTemp);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(
                getPos().getX(), getPos().getY(), getPos().getZ(),
                getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1
        );
    }

}
