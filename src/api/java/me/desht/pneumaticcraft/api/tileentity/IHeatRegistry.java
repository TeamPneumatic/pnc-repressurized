package me.desht.pneumaticcraft.api.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

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
     * <p>
     * Note: the preferred way to do this is via the BlockHeatProperties.cfg config file.
     *
     * @param block the block
     * @param temperature the block's temperature
     * @param thermalResistance the thermal resistance, i.e. how quickly heat will be transferred
     */
    void registerBlockExchanger(Block block, double temperature, double thermalResistance);

    /**
     * Register a block state as a heat exchanger.  This should only be used to distinguish block variants, never
     * rotation states.
     * <p>
     * Note: the preferred way to do this is via the BlockHeatProperties.cfg config file.
     *
     * @param state the blockstate
     * @param temperature the block variant's temperature
     * @param thermalResistance the thermal resistance, i.e. how quickly heat will be transferred
     */
    void registerBlockExchanger(IBlockState state, double temperature, double thermalResistance);

    /**
     * Register a heat behaviour instance.
     * <p>
     * Note: the preferred way to do this is via the BlockHeatProperties.cfg config file.
     *
     * @param heatBehaviour a heat behaviour
     */
    void registerHeatBehaviour(Class<? extends HeatBehaviour> heatBehaviour);
}
