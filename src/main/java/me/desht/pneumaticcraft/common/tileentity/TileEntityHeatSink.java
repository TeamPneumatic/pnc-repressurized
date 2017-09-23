package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityHeatSink extends TileEntityCompressedIronBlock implements IHeatExchanger {

    private final IHeatExchangerLogic airExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();

    public TileEntityHeatSink() {
        airExchanger.addConnectedExchanger(heatExchanger);
        airExchanger.setThermalResistance(14);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return side == null || side == getRotation() ? super.getHeatExchangerLogic(side) : null;
    }

    /**
     * Gets the valid sides for heat exchanging to be allowed. returning an empty array will allow any side.
     *
     * @return
     */
    @Override
    protected EnumFacing[] getConnectedHeatExchangerSides() {
        return new EnumFacing[]{getRotation()};
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return false;
    }

    @Override
    public void update() {
        super.update();
        airExchanger.update();
        airExchanger.setTemperature(295);
    }

    public void onFannedByAirGrate() {
        heatExchanger.update();
        airExchanger.setTemperature(295);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

}
