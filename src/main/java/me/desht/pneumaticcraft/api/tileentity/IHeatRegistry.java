package me.desht.pneumaticcraft.api.tileentity;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.block.Block;

public interface IHeatRegistry {
    IHeatExchangerLogic getHeatExchangerLogic();

    void registerBlockExchanger(Block block, double temperature, double thermalResistance);

    void registerHeatBehaviour(Class<? extends HeatBehaviour> heatBehaviour);
}
