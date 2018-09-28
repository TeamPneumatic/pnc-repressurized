package me.desht.pneumaticcraft.api.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.block.Block;

/**
 * Get an instance of this via {@link PneumaticRegistry.IPneumaticCraftInterface#getHeatRegistry()}.
 */
public interface IHeatRegistry {
    /**
     * Retrieve a heat exchanger logic instance.
     *
     * @return a heat exchanger logic
     */
    IHeatExchangerLogic getHeatExchangerLogic();

    /**
     * Register a block as a heat exchanger.
     *
     * @param block the block
     * @param temperature the block's temperature
     * @param thermalResistance the thermal resistance, i.e. how quickly heat will be transferred
     */
    void registerBlockExchanger(Block block, double temperature, double thermalResistance);

    /**
     * Register a heat behaviour instance.
     *
     * @param heatBehaviour a heat behaviour
     */
    void registerHeatBehaviour(Class<? extends HeatBehaviour> heatBehaviour);
}
