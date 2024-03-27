/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Allows PNC heat handling blocks to be heated by the IE External Heater
 */
record IEHeatHandler(BlockEntity blockEntity, Direction dir) implements ExternalHeaterHandler.IExternalHeatable {
    public static ExternalHeaterHandler.IExternalHeatable maybe(BlockEntity obj, Direction dir) {
        return obj instanceof IHeatExchangingTE ? new IEHeatHandler(obj, dir) : null;
    }

    public static void registerCap(RegisterCapabilitiesEvent event) {
        ModBlockEntityTypes.streamBlockEntities()
                .filter(be -> be instanceof IHeatExchangingTE)
                .forEach(be -> event.registerBlockEntity(ExternalHeaterHandler.CAPABILITY, be.getType(), IEHeatHandler::maybe));
    }

    @Override
    public int doHeatTick(int energyAvailable, boolean redstone) {
        return IOHelper.getCap(blockEntity, PNCCapabilities.HEAT_EXCHANGER_BLOCK, dir).map(handler -> {
            int rfPerTick = ConfigHelper.common().integration.ieExternalHeaterFEperTick.get();
            double heatPerRF = ConfigHelper.common().integration.ieExternalHeaterHeatPerFE.get();
            if (energyAvailable >= rfPerTick) {
                handler.addHeat(rfPerTick * heatPerRF);
                return rfPerTick;
            }
            return 0;
        }).orElse(0);
    }
}
