package me.desht.pneumaticcraft.api.heat;

import net.minecraft.block.Block;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when block heat properties are loaded from datapacks. This event gives an opportunity to add custom block
 * heat properties and behaviours in code.
 */
public class HeatRegistrationEvent extends Event {
    protected final IHeatRegistry registry;

    public HeatRegistrationEvent(IHeatRegistry registry) {
        this.registry = registry;
    }

    /**
     * Register a block as a heat exchanger.
     * <p>
     * Note: the preferred way (and only way if you want blockstate transitions) to do this is with datapack recipes.
     * See {@code data/pneumaticcraft/recipes/block_heat_properties/*.json}
     *
     * @param block the block
     * @param temperature the block's temperature
     * @param thermalResistance the thermal resistance, i.e. how quickly heat will be transferred
     */
    public void registerBlockExchanger(Block block, double temperature, double thermalResistance) {
        registry.registerBlockExchanger(block, temperature, thermalResistance);
    }
}
