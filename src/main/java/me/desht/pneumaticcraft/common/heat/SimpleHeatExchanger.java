package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import net.minecraft.util.EnumFacing;

public class SimpleHeatExchanger implements IHeatExchanger {
    private final IHeatExchangerLogic logic;

    public SimpleHeatExchanger(IHeatExchangerLogic logic) {
        this.logic = logic;
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return logic;
    }

}
