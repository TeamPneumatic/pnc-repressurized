package pneumaticCraft.common.tileentity;

import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityHeatSink extends TileEntityCompressedIronBlock implements IHeatExchanger{

    private final IHeatExchangerLogic airExchanger = PneumaticRegistry.getInstance().getHeatExchangerLogic();

    public TileEntityHeatSink(){
        airExchanger.addConnectedExchanger(heatExchanger);
        airExchanger.setThermalResistance(14);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(ForgeDirection side){
        return side == ForgeDirection.UNKNOWN || side == getRotation() ? super.getHeatExchangerLogic(side) : null;
    }

    /**
     * Gets the valid sides for heat exchanging to be allowed. returning an empty array will allow any side.
     * @return
     */
    @Override
    protected ForgeDirection[] getConnectedHeatExchangerSides(){
        return new ForgeDirection[]{getRotation()};
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate(){
        return false;
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
        airExchanger.update();
        airExchanger.setTemperature(295);
    }

    public void onFannedByAirGrate(){
        heatExchanger.update();
        airExchanger.setTemperature(295);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }

}
