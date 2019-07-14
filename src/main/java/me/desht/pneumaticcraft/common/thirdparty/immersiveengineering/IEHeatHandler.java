package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.tileentity.TileEntity;

import static me.desht.pneumaticcraft.common.config.Config.Common.Integration;

class IEHeatHandler {
    static void registerHeatHandler() {
        ExternalHeaterHandler.registerHeatableAdapter(TileEntityBase.class, new ExternalHeaterHandler.HeatableAdapter() {
            @Override
            public int doHeatTick(TileEntity tileEntity, int energyAvailable, boolean canHeat) {
                if (tileEntity instanceof IHeatExchanger && Integration.ieExternalHeaterHeatPerRF > 0 && !canHeat) {
                    IHeatExchangerLogic heatExchanger = ((IHeatExchanger) tileEntity).getHeatExchangerLogic(null);
                    if (heatExchanger != null && energyAvailable >= Integration.ieExternalHeaterRFperTick) {
                        heatExchanger.addHeat(Integration.ieExternalHeaterRFperTick * Integration.ieExternalHeaterHeatPerRF);
                        return Integration.ieExternalHeaterRFperTick;
                    }
                }
                return 0;
            }
        });
    }
}
