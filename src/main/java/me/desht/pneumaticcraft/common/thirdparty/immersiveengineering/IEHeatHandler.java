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
                    int rfPerTick = ConfigHelper.common().integration.ieExternalHeaterRFperTick.get();
                    double heatPerRF = ConfigHelper.common().integration.ieExternalHeaterHeatPerRF.get();
                    if (energyAvailable >= rfPerTick) {
                        handler.addHeat(rfPerTick * heatPerRF);
                        return rfPerTick;
                    }
                    return 0;
                }).orElse(0);
            }
        });
    }
}
