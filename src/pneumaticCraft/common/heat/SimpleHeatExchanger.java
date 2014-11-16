package pneumaticCraft.common.heat;

import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.tileentity.IHeatExchanger;

public class SimpleHeatExchanger implements IHeatExchanger{
    private final IHeatExchangerLogic logic;

    public SimpleHeatExchanger(IHeatExchangerLogic logic){
        this.logic = logic;
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(ForgeDirection side){
        return logic;
    }

}
