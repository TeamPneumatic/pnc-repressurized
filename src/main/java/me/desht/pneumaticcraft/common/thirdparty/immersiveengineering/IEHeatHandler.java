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
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.tileentity.TileEntity;

class IEHeatHandler {
    static void registerHeatHandler() {
        ExternalHeaterHandler.registerHeatableAdapter(TileEntityBase.class, new ExternalHeaterHandler.HeatableAdapter<TileEntity>() {
            @Override
            public int doHeatTick(TileEntity tileEntity, int energyAvailable, boolean canHeat) {
                return tileEntity.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).map(handler -> {
                    if (energyAvailable >= ConfigHelper.common().integration.ieExternalHeaterRFperTick.get()) {
                        handler.addHeat(ConfigHelper.common().integration.ieExternalHeaterRFperTick.get() * ConfigHelper.common().integration.ieExternalHeaterHeatPerRF.get());
                        return ConfigHelper.common().integration.ieExternalHeaterRFperTick.get();
                    }
                    return 0;
                }).orElse(0);
            }
        });
    }
}
