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

package me.desht.pneumaticcraft.api;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IFilteringItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

import java.util.Optional;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Public PneumaticCraft capability objects.
 */
public class PNCCapabilities {
    /**
     * Basic air handler; use this capability on entities which can be pressurized (drones by default)
     */
    public static final EntityCapability<IAirHandler,Void> AIR_HANDLER_ENTITY
            = EntityCapability.createVoid(RL("air_handler_entity"), IAirHandler.class);

    /**
     * Machine air handler; use this on tile entities which can store air.
     */
    public static final BlockCapability<IAirHandlerMachine, Direction> AIR_HANDLER_MACHINE
            = BlockCapability.createSided(RL("air_handler_machine"), IAirHandlerMachine.class);

    /**
     * Item air handler; use this on items which can be pressurized. See also
     * {@link me.desht.pneumaticcraft.api.item.IItemRegistry#makeItemAirHandler(ItemStack)} for an API
     * method to create a useful air handler implementation.
     */
    public static final ItemCapability<IAirHandlerItem,Void> AIR_HANDLER_ITEM
            = ItemCapability.createVoid(RL("air_handler_item"), IAirHandlerItem.class);

    /**
     * Heat exchanger capability
     */
    public static final BlockCapability<IHeatExchangerLogic,Direction> HEAT_EXCHANGER_BLOCK
            = BlockCapability.createSided(RL("heat_exchanger_block"), IHeatExchangerLogic.class);

    public static final EntityCapability<IHeatExchangerLogic,Void> HEAT_EXCHANGER_ENTITY
            = EntityCapability.createVoid(RL("heat_exchanger_entity"), IHeatExchangerLogic.class);

    /**
     * Item Filtering capability; may be attached to items which can act as filters in Drone programs and Logistics filters
     */
    public static final ItemCapability<IFilteringItem,Void> ITEM_FILTERING
            = ItemCapability.createVoid(RL("item_filtering"), IFilteringItem.class);

    /* ------------------------------------------------------------------------------------------------------- */

    /**
     * Convenience method to get the air handler for an item, if it exists.
     * @param stack the item stack to query
     * @return the optional air handler
     */
    public static Optional<IAirHandlerItem> getAirHandler(ItemStack stack) {
        return Optional.ofNullable(stack.getCapability(AIR_HANDLER_ITEM));
    }

    /**
     * Convenience method to get the air handler for a block entity, if it exists
     * @param blockEntity the block entity to query
     * @param direction the direction to check
     * @return the optional air handler
     */
    public static Optional<IAirHandlerMachine> getAirHandler(BlockEntity blockEntity, Direction direction) {
        return blockEntity.getLevel() == null ?
                Optional.empty() :
                Optional.ofNullable(blockEntity.getLevel().getCapability(AIR_HANDLER_MACHINE, blockEntity.getBlockPos(), direction));
    }

    /**
     * Convenience method to get the air handler for a block entity on the null "face", if it exists
     * @param blockEntity the block entity to query
     * @return the optional air handler
     */
    public static Optional<IAirHandlerMachine> getAirHandler(BlockEntity blockEntity) {
        return getAirHandler(blockEntity, null);
    }

    /**
     * Convenience method to get the air handler for an entity, if it exists
     * @param entity the entity to querty
     * @return the optional air handler
     */
    public static Optional<IAirHandler> getAirHandler(Entity entity) {
        return Optional.ofNullable(entity.getCapability(AIR_HANDLER_ENTITY));
    }

    public static Optional<IHeatExchangerLogic> getHeatLogic(BlockEntity blockEntity, Direction direction) {
        return Optional.ofNullable(blockEntity.getLevel().getCapability(HEAT_EXCHANGER_BLOCK, blockEntity.getBlockPos(), direction));
    }

    public static Optional<IHeatExchangerLogic> getHeatLogic(BlockEntity blockEntity) {
        return getHeatLogic(blockEntity, null);
    }

    public static Optional<IFilteringItem> getItemFiltering(ItemStack stack) {
        return Optional.ofNullable(stack.getCapability(ITEM_FILTERING));
    }
}
