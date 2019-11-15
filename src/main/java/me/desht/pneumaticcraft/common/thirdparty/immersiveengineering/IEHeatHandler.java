package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

class IEHeatHandler {
    static void registerHeatHandler() {
        // TODO 1.14
//        ExternalHeaterHandler.registerHeatableAdapter(TileEntityBase.class, new ExternalHeaterHandler.HeatableAdapter() {
//            @Override
//            public int doHeatTick(TileEntity tileEntity, int energyAvailable, boolean canHeat) {
//                if (tileEntity instanceof IHeatExchanger && Integration.ieExternalHeaterHeatPerRF > 0 && !canHeat) {
//                    IHeatExchangerLogic heatExchanger = ((IHeatExchanger) tileEntity).getHeatExchangerLogic(null);
//                    if (heatExchanger != null && energyAvailable >= Integration.ieExternalHeaterRFperTick) {
//                        heatExchanger.addHeat(Integration.ieExternalHeaterRFperTick * Integration.ieExternalHeaterHeatPerRF);
//                        return Integration.ieExternalHeaterRFperTick;
//                    }
//                }
//                return 0;
//            }
//        });
    }
}
