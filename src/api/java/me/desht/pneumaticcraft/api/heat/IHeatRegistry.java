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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Get an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getHeatRegistry()}.
 */
public interface IHeatRegistry {
    /**
     * Create a new ticking heat exchanger logic instance for use in block entities (or potentially other ticking objects)
     * that you create.
     *
     * @return a heat exchanger logic
     */
    IHeatExchangerLogic makeHeatExchangerLogic();

    /**
     * Register a block as a simple heat exchanger (temperature and thermal resistance only; no blockstate transitions).
     * Don't call this directly; subscribe to {@link HeatRegistrationEvent} or, preferably, use datapacks.
     *<p>
     * Note: the preferred way (and the only way if you want blockstate transitions) to do this is with datapack recipes.
     * See {@code data/pneumaticcraft/recipes/block_heat_properties/*.json}
     *
     * @param block the block
     * @param temperature the block's temperature
     * @param thermalResistance the thermal resistance, i.e. how quickly heat will be transferred
     * @deprecated removing in 1.19; this should be done by datapack
     */
    @Deprecated(forRemoval = true)
    void registerBlockExchanger(Block block, double temperature, double thermalResistance);

    /**
     * Register a heat behaviour instance. This can be called from a {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}
     * handler; do not use {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent#enqueueWork(Runnable)}.
     * Alternatively, if you need to override in-built behaviour (e.g. to disable automatic furnace fueling), you can
     * register a handler in a {@link net.minecraftforge.event.server.ServerAboutToStartEvent} handler.
     * <p>
     * This is intended to add custom behaviours to certain block entities, similar to how the vanilla furnace is handled.
     * For general blockstate transitions (on excess heat added/removed), the correct way to do this is with datapack
     * recipes. See {@code data/pneumaticcraft/recipes/block_heat_properties/*.json} for examples.
     *
     * @param id a unique for this heat behaviour
     * @param heatBehaviour a heat behaviour supplier
     */
    void registerHeatBehaviour(ResourceLocation id, Supplier<? extends HeatBehaviour> heatBehaviour);
}
