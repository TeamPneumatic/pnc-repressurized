package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityHeatSink extends TileEntityCompressedIronBlock implements IHeatExchanger {

    private final IHeatExchangerLogic airExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();

    private double ambientTemp = -1;

    public TileEntityHeatSink() {
        super(ModTileEntities.HEAT_SINK);

        airExchanger.addConnectedExchanger(heatExchanger);
        airExchanger.setThermalResistance(TileEntityConstants.HEAT_SINK_THERMAL_RESISTANCE);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(Direction side) {
        return side == null || side == getRotation() ? super.getHeatExchangerLogic(side) : null;
    }

    @Override
    protected Direction[] getConnectedHeatExchangerSides() {
        return new Direction[]{getRotation()};
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void tick() {
        if (ambientTemp < 0) {
            ambientTemp = HeatExchangerLogicAmbient.atPosition(getWorld(), getPos()).getTemperature();
            airExchanger.setTemperature(ambientTemp);
        }

        super.tick();

        airExchanger.tick();
        airExchanger.setTemperature(ambientTemp);
    }

    public void onFannedByAirGrate() {
        heatExchanger.tick();
        airExchanger.setTemperature(ambientTemp);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(
                getPos().getX(), getPos().getY(), getPos().getZ(),
                getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1
        );
    }

}
