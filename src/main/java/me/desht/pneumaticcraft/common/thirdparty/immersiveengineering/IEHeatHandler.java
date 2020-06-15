package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.config.PNCConfig.Common.Integration;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.tileentity.TileEntity;

class IEHeatHandler {
    static void registerHeatHandler() {
        ExternalHeaterHandler.registerHeatableAdapter(TileEntityBase.class, new ExternalHeaterHandler.HeatableAdapter() {
            @Override
            public int doHeatTick(TileEntity tileEntity, int energyAvailable, boolean canHeat) {
                return tileEntity.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).map(handler -> {
                    if (energyAvailable >= Integration.ieExternalHeaterRFperTick) {
                        handler.addHeat(Integration.ieExternalHeaterRFperTick * Integration.ieExternalHeaterHeatPerRF);
                        return Integration.ieExternalHeaterRFperTick;
                    }
                    return 0;
                }).orElse(0);
            }
        });
    }
}
