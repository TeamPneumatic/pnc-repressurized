package me.desht.pneumaticcraft.api.heat;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Supplier;

/**
 * Fired when block heat properties are loaded from datapacks. This event gives an opportunity to add custom block
 * heat properties and behaviours in code.
 */
public class HeatRegistrationEvent extends Event {
    private final IHeatRegistry registry;

    public HeatRegistrationEvent(IHeatRegistry registry) {
        this.registry = registry;
    }

    /**
     * Register a block as a heat exchanger.
     * <p>
     * Note: the preferred way to do this is with datapacks. See
     * {@code data/pneumaticcraft/pneumaticcraft/block_heat_properties/*.json}
     *
     * @param block the block
     * @param temperature the block's temperature
     * @param thermalResistance the thermal resistance, i.e. how quickly heat will be transferred
     */
    public void registerBlockExchanger(Block block, double temperature, double thermalResistance) {
        registry.registerBlockExchanger(block, temperature, thermalResistance);
    }

    /**
     * Register a heat behaviour instance. This can be used to add special behaviour to certain blocks, e.g.
     * the way the vanilla Furnace can be heated by a Vortex Tube.
     * <p>
     * For general blockstate transitions, the preferred way to do this is with datapacks. See
     * {@code data/pneumaticcraft/pneumaticcraft/block_heat_properties/*.json}
     *
     * @param id a unique for this heat behaviour
     * @param heatBehaviour a heat behaviour supplier
     */
    public void registerHeatBehaviour(ResourceLocation id, Supplier<? extends HeatBehaviour<?>> heatBehaviour) {
        registry.registerHeatBehaviour(id, heatBehaviour);
    }
}
