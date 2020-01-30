package me.desht.pneumaticcraft.api.heat;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

/**
 * Get an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getHeatRegistry()}.
 */
public interface IHeatRegistry {
    /**
     * Create a new ticking heat exchanger logic instance for use in tile entities (or potentially other ticking objects).
     *
     * @return a heat exchanger logic
     */
    IHeatExchangerLogic makeHeatExchangerLogic();

    /**
     * Register a block as a heat exchanger. Don't call this directly; subscribe to {@link HeatRegistrationEvent}.
     *
     * @param block the block
     * @param temperature the block's temperature
     * @param thermalResistance the thermal resistance, i.e. how quickly heat will be transferred
     */
    void registerBlockExchanger(Block block, double temperature, double thermalResistance);

    /**
     * Register a heat behaviour instance. Don't call this directly; subscribe to {@link HeatRegistrationEvent}.
     * <p>
     * Note: the preferred way to do this is with datapacks. See
     * {@code data/pneumaticcraft/pneumaticcraft/block_heat_properties/*.json}
     *
     * @param id a unique for this heat behaviour
     * @param heatBehaviour a heat behaviour supplier
     */
    void registerHeatBehaviour(ResourceLocation id, Supplier<? extends HeatBehaviour> heatBehaviour);
}
