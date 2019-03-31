package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.tileentity.TileEntity;

class IEHeatHandler {
    private static final int EXTERNAL_HEATER_RF_PER_TICK = 100;
    private static final double EXTERNAL_HEATER_HEAT_PER_RF = 0.01;

    static void registerHeatHandler() {
        ExternalHeaterHandler.registerHeatableAdapter(TileEntityBase.class, new ExternalHeaterHandler.HeatableAdapter() {
            @Override
            public int doHeatTick(TileEntity tileEntity, int energyAvailable, boolean canHeat) {
                if (tileEntity instanceof IHeatExchanger) {
                    IHeatExchangerLogic heatExchanger = ((IHeatExchanger) tileEntity).getHeatExchangerLogic(null);
                    if (heatExchanger != null && energyAvailable >= EXTERNAL_HEATER_RF_PER_TICK) {
                        heatExchanger.addHeat(EXTERNAL_HEATER_RF_PER_TICK * EXTERNAL_HEATER_HEAT_PER_RF);
                        return EXTERNAL_HEATER_RF_PER_TICK;
                    }
                }
                return 0;
            }
        });
    }
}
