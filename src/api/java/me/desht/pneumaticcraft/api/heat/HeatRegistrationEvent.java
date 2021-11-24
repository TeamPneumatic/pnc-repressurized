/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

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
